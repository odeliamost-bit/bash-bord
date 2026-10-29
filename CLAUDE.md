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

De map `ontwerp/` bevat het goedgekeurde ontwerp: 20 telefoonschermen (390 px breed).
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

- **Menu: zijbalk** links (gekozen door Odelia). De onderbalk uit `Main` valt weg; 🎤 en ➕ staan bij Onthoud.
- **Testen:** de app komt via een link op de telefoon (GitHub bouwt hem, zie README). Android Studio met kabel kan ook.
- **Plantje:** zelf getekend in code (`ui/thuis/Plantje.kt`). Achtergrond van de welkomkaart: eigen zacht verloop.
- **Versies** (AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Room 2.6.1, Glance 1.1.0) zijn bewust wat ouder: in de cloud is `dl.google.com` (ook Google Maven) geblokkeerd, dus Claude kon nieuwere versies niet controleren. GitHub Actions bouwt wel echt. Upgraden mag.
- **Checken in de cloud:** `hulpmiddelen/voorproef/` (compileren, testjes, schermen als plaatje) en GitHub Actions (echte Android-build). Dat vervangt testen op de telefoon niet.
- **Uren:** de 45 min pauze gaat er vanaf het begin af ("al 4¾ uur gewerkt" is zonder pauze). Weekdoel 35 uur (nog navragen: klopt dat?).
- **Herinneringen:** inklokken 7:50, uitklokken 15:50 (ma–vr), seintje voor een afspraak de dag ervoor om 16:15. Vaste tijden in `logica/Klok.kt` (`Instellingen`).
- **Urenmail:** de app opent Gmail met een **nieuwe** mail (onderwerp "Urenregistratie"). Antwoorden in de bestaande draad kan pas met de Gmail-koppeling (Google-account nodig). Nooit zelf versturen.
- **Beveiliging:** de telefoon krijgt de release-versie (niet debuggable), acties in het bouw-recept staan vast op een commit-SHA, minste rechten, Gradle-checksum, Dependabot elke zaterdag. CI controleert: alleen bekende Android-toestemmingen, netwerkcode alleen in `online/Online.kt`.
- **Online (gekozen door Odelia):** Zeilweer gaat alleen online na "ja" (`OnlineVraag`), alleen naar Open-Meteo (https, max 2 MB). De sync gaat alleen online na inloggen, alleen naar de eigen Supabase (zie hieronder). Opzoeken en route openen Chrome/Maps; de app zelf gaat dan niet online. De zeilplek staat op de telefoon, niet in de (openbare) code.
- **Weer-apps in Zeilweer:** knoppen naar Windy, Windfinder en Weeronline (gekozen door Odelia), met het echte icoontje van de telefoon. Logo's staan niet in de code. Lijst in `logica/Zeilweer.kt` (`weerApps`) én in `AndroidManifest.xml` (`<queries>`).
- **Leren (gekozen door Odelia, okt 2026):** eerder gebouwd dan de rest van fase 2. Vakken: Elektrotechniek, Burgerschap, Nederlands, Keuzedeel (= project/bewijsstuk). Kaarten met 5 groepen (G1–G5), quiz (meerkeuze + rekenen), overzicht, zelf kaarten maken (typen of 🎤). Inhoud in `logica/LerenInhoud.kt`, uit de Leerplek-pagina op claude.ai. Bewaard in `leren.json` op de telefoon (los van de database). Werkt zonder internet.
- **Pauze-seintje (Leren):** na 20 min leren, of als de laatste 3 antwoorden 2× zo traag zijn als de eerste 5 (en minstens 10 s). Keuze: 5 min pauze of "nog even door" (dan 10 min stil). De app kan niet echt zien of ze moe is; zeg dat eerlijk.
- **Leren met Claude** (uitleg-chat, kaarten laten maken): nog niet. Kan pas met een backend voor de sleutel (Odelia koos: sleutel via backend, nooit in de app).
- **Geen wachtwoorden (gekozen door Odelia, okt 2026):** de app slaat geen wachtwoorden op. Het Wachtwoorden-scherm is weggehaald, ook uit het ontwerp. Niet opnieuw bouwen.
- **Vaste handtekening:** via GitHub-geheim `APP_SLEUTEL` (zie README). Zolang die er niet is, gaan gegevens verloren bij een update.
- **Potjes (gekozen door Odelia, okt 2026):** vast eerst: Vaste lasten € 41,30, Zorg € 180, Snoep € 50. Rest: Vrije geld 20%, Investeren 30% (Giro), Goede doelen 10%, Noodpot 20%, Spaarpot 20% (ING-spaar). Saldo blijft staan. In `logica/GeldData.kt`.
- **Wat mag online (gekozen door Odelia, okt 2026):**
  - ☁️ Sync (eigen Supabase, alleen na inloggen): Geld, Onthoud (afspraken), Leren, Wensen, Recepten, Mensen, foto's, Instellingen (okt 2026; zonder grote tekst en tegels, die blijven per apparaat — Odelia wil op de tablet andere tegels, nog samen bespreken).
  - 🔒 Nooit online, alleen op het apparaat: **Uren, Cyclus, Mail-hulp, Advies, Weer, Thuis.** Niet synchroniseren tussen telefoon en tablet.
  - Lijst staat in `logica/SyncRegels.kt` (`ONLINE`), in `supabase/kompas.sql` (check) én in de CI-controle. Altijd alle drie samen aanpassen, en alleen als Odelia het vraagt.
- **Sync (okt 2026):** eigen Supabase-project. Inloggen met code per e-mail (geen wachtwoord). Eén rij per onderdeel (JSON), nieuwste wijziging wint (tijd van de server). Realtime via websocket zolang de app open is; bij openen alles bijwerken. Netwerkcode alleen in `online/Online.kt` (weer) en `online/Supabase.kt` (sync). Sleutels via GitHub-geheimen `SUPABASE_URL` en `SUPABASE_ANON_KEY` (zie README). Inlog en sync-stand in `noBackupFilesDir`. Eerste inlog op een apparaat: online wint, oude data in `reserve-<onderdeel>.json`.
- **Google Agenda (gewenst, okt 2026):** afspraken van vandaag en deze week op het dashboard, alleen lezen. Plan: de agenda van de telefoon lezen (Android `CalendarContract`, toestemming `READ_CALENDAR`), geen Google-login nodig. Nog niet gebouwd.

- **Kompas samenvoegen (gekozen door Odelia, okt 2026):** de oude Kompas-pagina (claude.ai) gaat op in bash-bord, met de opmaak van bash-bord. Overnemen: Papieren (document → samenvatting + actiepunten), Nu doen-kaart, grote tekst, back-up. Kompas-taken en -wensen worden ingelezen uit Kompas' eigen back-up (`logica/KompasImport.kt`). Taak-in-stapjes niet overnemen.
- **Instellingen (okt 2026):** menu "Instellingen" met 2 tabjes: ⚙️ Instellingen | 💾 Back-up (`ui/instellingen/`). Instellingen: grote tekst (+20%), herinneringstijden, weekdoel, meldingen aan/uit, tegels op Thuis (aan/uit + volgorde), menu-knoppen verbergen (Thuis en Instellingen altijd). Bewaard in `instellingen.json` (`logica/InstellingenData.kt`), alleen op het apparaat, wel in de back-up. De vaste waarden in `logica/Klok.kt` (`Instellingen`) zijn nu alleen de beginwaarden.
- **Back-up (okt 2026):** menu "Back-up" (`ui/instellingen/`). Eén .zip met database, alle JSON, foto's, instellingen en cyclus; niet de sync-inlog. Odelia kiest zelf waar het bestand komt. Terugzetten vervangt alles en start de app opnieuw (`data/Backup.kt`).

## Status

- Stap 1 (basis-app): gebouwd.
- Stap 2–5 (klokken, onthoud, meldingen, urenmail als nieuwe Gmail-mail): gebouwd, GitHub bouwt de app. **Nog niet getest op de telefoon.**
- Extra: beveiliging, opzoeken/route via Chrome/Maps, Zeilweer (5 weermodellen, met toestemming online). **Nog niet getest op de telefoon.**
- Leren (kaarten, quiz, overzicht, eigen kaarten, pauze-seintje): gebouwd op tak `claude/leren`. **Nog niet getest op de telefoon.**
- Alle schermen uit het ontwerp (Geld, Cyclus, Wensen, Mensen, Mail-hulp, Advies, Recepten, Leren-extra's, foto bij Onthoud, menu direct) gebouwd op tak `claude/alle-schermen` (okt 2026). Elk scherm eigen JSON-bestand; Room niet veranderd. Geen AI (vaste teksten). **Nog niet getest op de telefoon.**
- Versie 0.3: echte potjes-bedragen (deel 2 van `SYNC-EN-POTTEN.md`). Geldt alleen als Geld nog leeg is; staat er al iets in `geld.json`, dan past Odelia de potjes in de app aan. **Nog niet getest op de telefoon.**
- Versie 0.4: sync stap 1+2 (inloggen + Geld via Supabase, realtime). Compileert en testjes slagen in de cloud; **nog niet getest met echte Supabase en niet op de telefoon.**
- Kompas stap 1: back-up maken/terugzetten + Kompas-bestand inlezen, op tak `claude/kompas-samen`. **Nog niet getest op de telefoon.**
- Instellingen-scherm (tabjes, grote tekst, tijden, weekdoel, meldingen, tegels, menu). **Nog niet getest op de telefoon.**
- Kompas stap 2–4 (nog te doen): Nu doen-kaart, Papieren.
- Sync stap 3: Onthoud (+Mensen), Wensen, Leren, Recepten, Instellingen via Supabase. ☁️-kaart ook bij Instellingen. Supabase-SQL moet één keer opnieuw (`instellingen` in de check). **Nog niet getest met echte Supabase en niet op de telefoon.**
- Nog te doen: sync voor foto's; tegels op de tablet bespreken; webpagina voor de laptop; Google Agenda; Gmail-koppeling (concept in de draad), telefoon op stil bij inklokken (fase 2-wens).
