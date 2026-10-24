# Voorproef 🧪

Hulpje voor Claude in de cloud. **Niet nodig voor de app zelf.**

In de cloud-omgeving is `dl.google.com` (Android-gereedschap en Google Maven) geblokkeerd.
Daarom kan de echte Android-app daar niet gebouwd worden.

Dit hulpje gebruikt de computer-versie van Compose (Compose Multiplatform, van Maven Central) om:

1. de Compose-code van de app te **compileren** (dezelfde Compose-versie als de app, 1.7);
2. de schermen als **plaatjes** te tekenen (met Compose 1.5, die geen Google-onderdelen nodig heeft).

`MainActivity`, `Voorlezer` en `Voorbeelden` doen niet mee: die bestaan alleen op Android.
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
