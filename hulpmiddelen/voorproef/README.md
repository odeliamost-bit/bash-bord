# Voorproef 🧪

Hulpje voor Claude in de cloud. **Niet nodig voor de app zelf.**

Eerst was `dl.google.com` in de cloud geblokkeerd. Sinds 24 okt kan de echte app daar wel gebouwd worden
(`hulpmiddelen/cloud/bouw.sh`). Een emulator kan er niet draaien, dus voor **plaatjes** blijft dit hulpje handig.

Dit hulpje gebruikt de computer-versie van Compose (Compose Multiplatform, van Maven Central) om:

1. de Compose-code van de app te **compileren** (dezelfde Compose-versie als de app, 1.7);
2. de schermen als **plaatjes** te tekenen (met Compose 1.5, die geen Google-onderdelen nodig heeft).

`MainActivity`, `Voorlezer`, `Voorbeelden`, de ViewModel en de mappen `data/`, `widget/` en `meldingen/` doen niet mee: die bestaan alleen op Android.
Kleine nep-versies van `R`, `painterResource`, `Font` en `BackHandler` staan in `src/`.

Let op: het blijft een **voorproef**. Letters en schaduwen zien er op de telefoon iets anders uit.
Echt testen kan alleen op de telefoon.

## Gebruik

```sh
cd hulpmiddelen/voorproef
gradle compileKotlin                 # code-check (Compose 1.7.3)
gradle teken -Pcmp=1.5.12 -Puit=/pad # plaatjes tekenen
```

Geeft Maven Central steeds 404/429? Start dan eerst de spiegel die downloads opnieuw probeert,
en voeg `-Pspiegel=http://127.0.0.1:8765` toe:

```sh
python3 -I maven_spiegel.py 8765 &
```
