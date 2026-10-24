"""Lokale Maven-spiegel die downloads opnieuw probeert (Maven Central geeft hier wisselend 404/429)."""
import http.server
import os
import socketserver
import sys
import time
import urllib.error
import urllib.request

BRONNEN = ["https://repo.maven.apache.org/maven2", "https://plugins.gradle.org/m2"]
MAP = os.path.join(os.path.dirname(os.path.abspath(__file__)), "maven-cache")


def haal(pad):
    doel = os.path.join(MAP, pad.lstrip("/"))
    if os.path.isfile(doel):
        return open(doel, "rb").read()
    if os.path.isfile(doel + ".ontbreekt"):
        return None
    for bron in BRONNEN:
        nietGevonden = 0
        for poging in range(12):
            try:
                with urllib.request.urlopen(bron + pad, timeout=60) as r:
                    data = r.read()
                os.makedirs(os.path.dirname(doel), exist_ok=True)
                with open(doel, "wb") as f:
                    f.write(data)
                return data
            except urllib.error.HTTPError as e:
                if e.code == 404:
                    nietGevonden += 1
                    if nietGevonden >= 10:
                        break
                time.sleep(min(0.3 * (poging + 1), 3))
            except Exception:
                time.sleep(1)
    os.makedirs(os.path.dirname(doel), exist_ok=True)
    open(doel + ".ontbreekt", "w").close()
    return None


class Handler(http.server.BaseHTTPRequestHandler):
    def _antwoord(self, met_inhoud):
        data = haal(self.path.split("?")[0])
        if data is None:
            self.send_response(404)
            self.end_headers()
            return
        self.send_response(200)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        if met_inhoud:
            self.wfile.write(data)

    def do_GET(self):
        self._antwoord(True)

    def do_HEAD(self):
        self._antwoord(False)

    def log_message(self, *args):
        pass


class Server(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True


if __name__ == "__main__":
    poort = int(sys.argv[1]) if len(sys.argv) > 1 else 8765
    Server(("127.0.0.1", poort), Handler).serve_forever()
