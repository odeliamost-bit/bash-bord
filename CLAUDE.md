# Mijn dashboard: projectbrief

Dit bestand leest Claude Code vanzelf bij elke sessie. Het beschrijft wat we bouwen en hoe we samenwerken.

## Voor wie

De app is voor Odelia. Ze bouwt hem samen met haar broer, die de laptop bedient.
Odelia heeft licht autisme. Lezen en typen kosten haar veel energie, luisteren en praten minder.

**Zo praat je met ons:**
- Nederlands.
- Korte zinnen. Eén ding tegelijk.
- Plaatjes, emoji en lijstjes in plaats van lange lappen tekst.
- Leg uit wat je gaat doen voordat je het doet.
- Wees eerlijk. Heb je iets niet geprobeerd of getest? Zeg dat. Zeg nooit dat iets niet kan als je het niet geprobeerd hebt.

## Belangrijkste regels

1. **Vraag toestemming voordat je code schrijft of iets groots verandert.** Eerst een plan, dan "mag ik?", dan pas bouwen.
2. Bouw in kleine stappen. Na elke stap moet de app op de telefoon te testen zijn.
3. **Nooit zelf e-mail versturen.** De app zet alleen een concept klaar.
4. **Nooit echte wachtwoorden** in code, testdata of git. Alleen verzonnen voorbeelden.
5. Geen geheime sleutels (API keys) in git zetten.

## Wat we bouwen

Een persoonlijk dashboard op de telefoon: vooral een **extern geheugen** (afspraken onthouden en terugvinden) en een **urenregistratie**.

**Apparaten:** Nothing Phone (1) met Android 15, en een Android-tablet. Gmail als mail.

**Techniek (voorstel uit eerder overleg, mag je kritisch bekijken):**
- Native Android: Kotlin + Jetpack Compose.
- Widgets op het startscherm met Glance.
- Data lokaal op de telefoon met Room.
- Datamodel klaar voor later synchroniseren: elk record een UUID en `updated_at`.
- Installeren als APK (geen Play Store).
- Later misschien een backend (Cloudflare Workers + D1). Nu nog niet.

## Fase 1: hier beginnen we

In deze volgorde, tenzij Odelia iets anders kiest:

1. **Basis-app** met de stijl, het menu (zijbalk) en het hoofdscherm.
2. **In- en uitklokken**
   - Grote in/uit-knop, ook als widget op het startscherm. Kleur verandert bij in/uit.
   - Onder de knop: "al 5½ uur gewerkt".
   - Pauze: vast 45 minuten per dag eraf.
   - Overzicht per dag en per week (ring + staafjes).
   - Herinnering om in en uit te klokken.
   - Werktijden meestal: start 7:30–8:00, einde ca. 15:45. Werkt ma–vr.
3. **Onthoud (extern geheugen)**
   - Afspraken maakt ze meestal mondeling. Ze wil ze snel vastleggen, het liefst door **in te spreken**.
   - Per afspraak: wie, wat, waar, wanneer (vrij, geen strak lijstje velden).
   - Kiezen per persoon, per organisatie (werk, school) of per project (bv. PVB, keuzedeel).
   - **Zoekbalk** om alles terug te vinden.
   - Samenvatting per onderwerp en per persoon.
   - Deadlines: melding de dag ervoor.
   - Share-knop (vanuit andere apps iets naar de app delen) en plak-knop.
   - Quick-add widget.
4. **Meldingen**
   - Ochtendmelding 20 minuten na inklokken: wat staat er vandaag/deze week.
5. **Urenmail klaarzetten**
   - Uren gaan naar Kim van Randstad (werk: Cargill BioIndustrial).
   - Vrijdagmiddag ca. 30 min na uitklokken een **concept** klaarzetten in Gmail, als antwoord in de bestaande draad "Urenregistratie". Plus een herinnering. Nooit zelf versturen.
   - Tekst:
     ```
     Hoi Kim,

     Hier de uren van afgelopen week.
     [eventuele opmerking]

     Met hartelijke groet,
     Odelia van der Most
     ```
     Daaronder een tabel ma–vr: datum, begin, eind, uren. "vrij" of "ziek" als tekst.
   - Hoe het concept in Gmail komt (Gmail API, of eerst gewoon een deel-knop) moet nog uitgezocht worden. Leg de opties eenvoudig uit.

## Later (fase 2 en verder)

Die schermen staan al in het ontwerp, maar komen pas na fase 1:
Mensen, Mail-hulp (chat die voorbeeldmails schrijft), Advies, Leren, Cyclus, Geld (potjes, uitgaven, verdelen), Wensen, Recepten, Zeilweer.

Wensen die nog uitgezocht moeten worden (eerlijk bespreken of het kan):
- Geld: ING- en Google Pay-meldingen lezen en uitgaven automatisch invullen.
- Koppeling met Google Sheets.
- Telefoon op stil zetten bij inklokken.

## Het ontwerp

De map `ontwerp/` bevat het goedgekeurde ontwerp: 18 telefoonschermen (390 px breed).
Het zijn `.dc.html`-bestanden uit een ontwerp-tool. Ze openen **niet** goed als gewone webpagina, maar de HTML en CSS laten precies zien hoe het eruit moet zien: kleuren, afmetingen, teksten, knoppen. `canvas.json` geeft de naam van elk scherm.

| Bestand | Scherm |
|---|---|
| Main | Hoofdscherm (tabjes Vandaag / Werk / Onthoud) |
| Widget | Widget op startscherm |
| Toevoegen | Onthoud: snel vastleggen |
| Navigatie / NavigatieOpen | Menu dicht / open |
| Mensen | Mensen + afspraken + zoeken |
| Cyclus, Geld*, Mail, Advies, Leren, Wensen, Recepten, Weer | Latere schermen |

De kleine bestanden (zoals `GeldGekocht`) tonen alleen een andere stand van een groot scherm.

**Stijl (houden zoals het ontwerp):**
- Achtergrond: zacht verloop perzik → crème (`#F4B29C` → `#FDF6EF`).
- Kaarten: crème `#FBF3EA`, heel rond (ca. 26–30 px), zachte 3D-schaduw, "klei"-gevoel.
- Hoofdkleur koraal (`#F4A58B` → `#D9694F`). Pastels: roze, geel, blauw, lila, salie-groen.
- Lettertype: Nunito, dik (800–900).
- Grote getallen, grote tik-knoppen (min. 48 px), icoontjes bij alles.
- Uren-ring: dunne donut in felle pastels, legenda met vierkantjes.
- Menu: zijbalk links met gekleurde 3D-icoontjes zonder blokjes eromheen.
- Voorleesknop (🔊) bij teksten.

**Hoofdscherm-kaart bovenaan:** "Goedemorgen, Odelia!" met haar eigen meisje-plaatje links (hoofd steekt boven de kaartrand uit) en een plantje rechts op een hangend plankje. Haar plaatje staat in `plaatjes/`. Het plantje en de achtergrond in het ontwerp komen uit een voorbeeld van internet; voor de echte app is een eigen plaatje nodig.

`plaatjes/mijn-wensen.png` is een overzicht van al haar wensen.

## Gemaakte keuzes

- **Menu: zijbalk** links (gekozen door Odelia). De onderbalk uit `Main` valt weg; de 🎤- en ➕-knoppen daarvan staan nu onder het Onthoud-tabje.
- **Zijbalk gedrag:** tik op een icoontje = meteen naar dat scherm, de zijbalk blijft smal. Alleen het oranje knopje bovenaan maakt hem breed (met namen); nog een keer = weer smal.
- **Tik-effect:** geen standaard Android-vlek (ripple). Een knop wordt bij tikken heel even iets kleiner. Gebruik voor nieuwe knoppen `tik` of `kies` uit `ui/onderdelen/Tik.kt`.
- **Icoontjes:** Geld is een munt met €-teken (in het ontwerp stond een $). Paden die in het ontwerp net uit het midden stonden, zijn rechtgezet met `verschuif`.
- **Testen:** de broer bouwt in Android Studio en drukt op ▶️ (telefoon met kabel).
- **Plantje:** zelf getekend in code (`ui/thuis/Plantje.kt`). Achtergrond van de welkomkaart: eigen zacht verloop.
- **Versies** (AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01) zijn bewust wat ouder: eerst was `dl.google.com` in de cloud geblokkeerd. Sinds 24 okt werkt het wel. Upgraden mag later, als aparte stap.
- **Bouwen in de cloud:** `hulpmiddelen/cloud/bouw.sh` zet de Android SDK klaar en bouwt de echte APK. Maven Central hapert hier soms, daarom gaan downloads via `maven_spiegel.py`.
- **Plaatjes in de cloud:** schermen met `hulpmiddelen/voorproef/` (computer-versie). Widgets met Robolectric: `WidgetPlaatjeTest` schrijft naar `app/build/plaatjes/`. Een emulator kan hier niet (geen KVM). Het vervangt testen op de telefoon niet.
- **Opbouw van de code:**
  - rekenregels zonder Android in `uren/` en `onthoud/` (met unit-tests in `app/src/test`)
  - schermen in `ui/` (krijgen gegevens en acties mee, zodat de voorproef ze kan tekenen)
  - Android-delen in `data/` (Room), `widget/` (Glance) en `meldingen/` (wekkers en meldingen). De voorproef slaat die over.
  - na elke wijziging in de gegevens: `Bijwerken.alles` (widgets verversen, oude meldingen weg)
- **Stap 2 (keuzes van 24 okt):**
  - Pauze: altijd 45 minuten eraf, ook op een korte dag.
  - Weekdoel: voorlopig 35 uur. Odelia weet het niet zeker, dus het moet aan te passen zijn.
  - Herinneringen ma–vr: 8:15 als ze nog niet is ingeklokt, 16:15 als ze nog niet is uitgeklokt.
  - Bij de uitklok-herinnering: snooze-knoppen "over 10 min" en "over 20 min" (ze werkt soms tot 16:30).
- **Keuzes van Claude bij stap 2–5** (24 okt; Odelia moet ze nog bevestigen, zie LOGBOEK.md):
  - Tegel "Vandaag": tijdens het werk de tijd sinds inklokken (zoals het ontwerp), na uitklokken de uren min pauze.
  - Vrij en ziek = 0 uur, telt niet mee voor het weekdoel.
  - Vergeten uit te klokken: de app stelt 15:45 voor.
  - Seintje voor afspraken: de dag ervoor om 16:30.
  - "Waar" heeft geen eigen veld (komt in de tekst of notitie). Groepen vast: School, Werk, Thuis. Projecten maakt ze zelf.
  - Urenmail: Gmail openen met de tekst, of kopiëren voor een antwoord in de draad. Uren als "7,25", datum "19-10-2026". Het adres van Kim staat alleen op de telefoon. Echte Gmail-koppeling: zie `GMAIL-KOPPELING.md`.

## Status

- Stap 1 (basis-app: stijl, zijbalk, hoofdscherm met voorbeeld-getallen): gebouwd en getest op de telefoon. 👍
- Verbeteringen na die test (zijbalk, tik-effect, icoontjes, Wachtwoorden weg): gebouwd. Nog niet getest op de telefoon.
- Stap 2 t/m 5 van fase 1 (klokken, Onthoud, ochtendmelding, urenmail): gebouwd door Claude op 24 okt (zelfstandig, met toestemming). Echte app gebouwd en getest met unit-tests en Robolectric. **Nog niet getest op de telefoon.** Zie `LOGBOEK.md`.
- Open vragen voor Odelia staan in `LOGBOEK.md` (bij ❓).
- Let op bij installeren: de APK uit de cloud heeft een andere sleutel dan de versie uit Android Studio. Eerst de oude app verwijderen (zie README).
