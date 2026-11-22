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
- **Plantje:** zelf getekend in code (`ui/thuis/Plantje.kt`), okt 2026 nagetekend naar het plantje dat Odelia koos: brede olijfgroene blaadjes in een rond zandkleurig potje. Geen plaatje van internet in de (openbare) code. Achtergrond van de welkomkaart: eigen zacht verloop.
- **Menu in 7 groepen (gekozen door Odelia, okt 2026):** Thuis, Werk (uren, mail-hulp), Onthoud (+ mensen), Leren, Geld (+ wensen), Voor mij (cyclus, recepten, zeilweer, advies), Instellingen (`ui/menu/Groep.kt`). Zijbalk roze, icoontjes in het midden. Tik op icoon = meteen erheen (bij meer plekken een tegelscherm, `GroepScherm.kt`); de pijl-knop schuift de zijbalk uit met namen. Menu-knoppen verbergen in Instellingen werkt nog per plek.
- **Leren-tegels + Simulator (gewenst, okt 2026):** Leren wordt één tegelscherm (Simulator nieuw, Kaarten, Quiz, Toetsen, Tutor, Maken, Formules, Overzicht). Gebouwd (`ui/leren/LerenTegels.kt`, `SimulatorScherm.kt`, `logica/Simulator.kt`): Simulator met groepenkast (schakelaars om, lampjes uit), stroomkring (S1, zekering, A- en V-meter, wet van Ohm), motor met start/stop en zelfhouding, aderkleuren, en Formules. Teksten uit de Kompas Elektrotechniek-pagina. Toetsen (okt 2026): toets plannen met naam, vak, datum en stof, lijst met "over N dagen" (`ui/leren/ToetsenScherm.kt`, bewaard in `leren.json` als `toetsen`, dus ook in sync en back-up). Tutor toont nog "komt nog" (kan pas met backend). De Simulator-tegel op Thuis opent nu de Simulator in de app. **Tekenen (okt 2026):** eerste tabje van de Simulator = de eerdere Schakelbord-simulator (claude.ai), ingebouwd als `assets/schakelbord.html` in een WebView zonder netwerk (`ui/leren/Tekenvlak.kt`; voorproef gebruikt `TekenvlakNep.kt`). Rekent echt (knooppuntmethode). Erbij gekomen: bron DC/AC tot 400 V (AC rekent met RMS), regelbare weerstand, motor tot 4 kW, hoofdcontacten 3-polig (1-2/3-4/5-6) bij een spoel, hulpcontacten NO/NC, knop Idee (ideeën + suggesties, alleen op het apparaat). Daarna erbij (okt 2026): bedieningskast (noodstop die vast blijft, draaischakelaar 0-1, sleutelschakelaar, signaallamp oranje), tijdrelais (aan-/afvalvertraagd), installatieautomaat B/C (thermisch + magnetisch, weer omhoog zetten), motorbeveiligingsschakelaar (Ir instelbaar), thermisch relais (95-96 / 97-98, reset). Contacten koppel je via "Hoort bij spoel" aan spoel, tijdrelais, motorbeveiliging of thermisch relais. Lettercodes S, K, F, Q, H, M en klemnummers volgens de norm. Nog niet: echte driefasen (faseverschuiving), trafo. **Nog niet getest op de telefoon.**
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
  - ☁️ Sync (eigen Supabase, alleen na inloggen, **versleuteld**): Geld, Onthoud (afspraken), Leren, Wensen, Recepten, Mensen, foto's, Instellingen (okt 2026; zonder grote tekst en tegels, die blijven per apparaat — Odelia wil op de tablet andere tegels, nog samen bespreken). Nov 2026 (bouwnacht 1, gekozen door Odelia): ook **Advies** (⭐-favorieten) en **Weer** (de zeilplek). Mail-hulp en Thuis wilde ze ook, maar die bewaren zelf niets (Thuis-tegels blijven per apparaat), dus daar is niets te synchroniseren.
  - 🔒 Nooit online, alleen op het apparaat: **Uren, uurloon, Cyclus**, en ook Beleggen, ING-meldingen-instellingen, logboek en Claude-kosten. Niet synchroniseren tussen telefoon en tablet.
  - Lijst staat in `logica/SyncRegels.kt` (`ONLINE`), in `supabase/kompas.sql` (check) én in de CI-controle. Altijd alle drie samen aanpassen, en alleen als Odelia het vraagt.
- **Sync (okt 2026):** eigen Supabase-project. Inloggen met code per e-mail (geen wachtwoord). Eén rij per onderdeel (JSON), nieuwste wijziging wint (tijd van de server). Realtime via websocket zolang de app open is; bij openen alles bijwerken. Netwerkcode alleen in `online/Online.kt` (weer) en `online/Supabase.kt` (sync). Sleutels via GitHub-geheimen `SUPABASE_URL` en `SUPABASE_ANON_KEY` (zie README). Inlog en sync-stand in `noBackupFilesDir`. Eerste inlog op een apparaat: online wint, oude data in `reserve-<onderdeel>.json`.
- **Versleutelde sync (bouwnacht 1, nov 2026):** alles gaat versleuteld (AES-256-GCM) naar Supabase (`logica/Versleutel.kt`). Odelia kiest één **geheime zin** (min. 8 tekens) en typt die op elk apparaat één keer (kaart ☁️ bij Instellingen/Geld). Sleutel = PBKDF2(zin, haar Supabase-id); de zin zelf wordt nergens bewaard, de sleutel staat versleuteld met de Android-sleutelkluis in `noBackupFilesDir` (`data/SyncSleutel.kt`). Zonder zin synct er niks. Verkeerde zin = niks overschrijven, melding. Oude onversleutelde rijen worden gelezen en meteen versleuteld teruggezet. Uitloggen wist de sleutel. Zin kwijt = online gegevens niet meer leesbaar (op het apparaat staat alles nog). Toegang tot de rijen alleen met haar eigen login (RLS, al zo). **Privacy-overzicht** bij Instellingen (`ui/instellingen/PrivacyKaart.kt`, `logica/Privacy.kt`): per onderdeel 📱 of 🔒.
- **Google Agenda (gewenst, okt 2026):** afspraken van vandaag en deze week op het dashboard, alleen lezen. Plan: de agenda van de telefoon lezen (Android `CalendarContract`, toestemming `READ_CALENDAR`), geen Google-login nodig. Nog niet gebouwd.

- **Kompas samenvoegen (gekozen door Odelia, okt 2026):** de oude Kompas-pagina (claude.ai) gaat op in bash-bord, met de opmaak van bash-bord. Overnemen: Papieren (document → samenvatting + actiepunten), Nu doen-kaart, grote tekst, back-up. Kompas-taken en -wensen worden ingelezen uit Kompas' eigen back-up (`logica/KompasImport.kt`). Taak-in-stapjes niet overnemen.
- **Instellingen (okt 2026):** menu "Instellingen" met 2 tabjes: ⚙️ Instellingen | 💾 Back-up (`ui/instellingen/`). Instellingen: grote tekst (+20%), herinneringstijden, weekdoel, meldingen aan/uit, tegels op Thuis (aan/uit + volgorde), menu-knoppen verbergen (Thuis en Instellingen altijd). Bewaard in `instellingen.json` (`logica/InstellingenData.kt`), alleen op het apparaat, wel in de back-up. De vaste waarden in `logica/Klok.kt` (`Instellingen`) zijn nu alleen de beginwaarden.
- **Back-up (okt 2026):** menu "Back-up" (`ui/instellingen/`). Eén .zip met database, alle JSON, foto's, instellingen en cyclus; niet de sync-inlog. Odelia kiest zelf waar het bestand komt. Terugzetten vervangt alles en start de app opnieuw (`data/Backup.kt`).

- **Beleggen (gekozen door Odelia, nov 2026):** tegel "Beleggen" onder de groep Geld (`ui/beleggen/`, `logica/Beleggen.kt`). Advies voor Ahold (AD.AS): kopen/houden/verkopen met redenen (trend 20/50 dagen, RSI, koersdoel analisten, stop-verlies 10%, helft verkopen bij +15%, 7 rustdagen, kosten-check). Echte pot (DEGIRO, zelf invullen) en oefenpot (€400 nep). Begint met 4 tegels (Odelia wilde geen krappe tabjes): Advies nu, Mijn potten, Ideeën (andere aandelen + ETF-tips), Test en instellingen. De app plaatst **nooit** zelf orders. Koers ophalen alleen na "ja", alleen bij Yahoo Finance (`query1.finance.yahoo.com`), of zelf intikken. Bewaard in `beleggen.json` in `noBackupFilesDir`: alleen op de telefoon, niet in de sync. **Geen echte bedragen of aantallen van Odelia in de code: de repo is openbaar.** Dagelijkse check + pushmelding doet Claude (geplande taak), niet de app.

- **Poort-check (bouwnacht 2, gevraagd door Odelia, nov 2026):** naast het vangnet. Staat **standaard uit** (kaart "📍 Poort-check" bij Instellingen). Odelia zet hem aan, staat één keer bij de poort en tikt "Ik sta nu bij de poort", en kiest in Android "Altijd toestaan". Daarna: alleen op werkdagen, alleen zolang ze ingeklokt is, vanaf 16:00 om de 17 minuten tot het vangnet (18:00) **één** korte plek-vraag (netwerk, anders GPS; `data/Locatie.kt`, geen Google-diensten). Verder dan 250 m (min de onnauwkeurigheid) = buiten → melding met zoempje "📍 Je bent buiten de poort" + knop Uitklokken. Plek van de poort alleen in `poort.json` (`noBackupFilesDir`): niet online, niet in de back-up, nooit in de code. Nieuwe toestemmingen: `ACCESS_COARSE/FINE/BACKGROUND_LOCATION` (ook in de CI-lijst). Uitklok-meldingen gaan nu via een eigen kanaal "Uitklokken (zoempje + geluid)" met trilpatroon en geluid.
- **Uitklok-vangnet (gekozen door Odelia, nov 2026):** `logica/Vangnet.kt`. Nog ingeklokt na de herinnering (15:50)? Dan komt de melding nog 2 keer (na 15 en 30 min). Om 18:00 klokt de app zelf uit op de gewone eindtijd (5 min vóór de herinnering = 15:45) en vraagt "Klopt dat?" (knop Klopt, of tikken → Werk-tab om de dag aan te passen). Stond er de volgende ochtend nog iets open, dan gebeurt hetzelfde bij de inklok-herinnering. Geen locatie, geen wifi (Odelia zit op werk op 5G): kost geen batterij.
- **Vertalen (gekozen door Odelia, nov 2026):** tegel "Vertalen" onder Voor mij, widget "Vertalen" (typen, plakken, inspreken) en "Vertalen (Bash-bord)" in het menuutje bij geselecteerde tekst (`logica/Vertalen.kt`, `ui/vertalen/`, `widget/VertaalWidget.kt`). Bovenaan 2 keuzes: Gewone tekst of Appje (kort, spreektaal, emoji blijven). Typen en plakken: de app zet de vraag klaar in de Claude-app (`com.anthropic.claude`, delen als tekst) en Odelia drukt zelf op versturen.
- **Vertalen via backend (gekozen door Odelia, nov 2026):** widget-knop 🎤 Vertalen → Google-spraakvenster (onzichtbaar schermpje `widget/SpreekVertaal.kt`, de app opent niet) → eigen Cloudflare Worker (`backend/vertaler/`, Claude-sleutel alleen daar als geheim) → zwart balkje onderaan over andere apps (`widget/VertaalBalk.kt`, toestemming `SYSTEM_ALERT_WINDOW`, één keer gevraagd; anders een Toast). Netwerkcode in `online/Vertaler.kt`, alleen naar `VERTAAL_URL`. GitHub-geheimen `VERTAAL_URL` en `VERTAAL_CODE` (zie README). Geen sleutel in de app.
- **Geld uit ING-meldingen (gekozen door Odelia, nov 2026):** de app leest mee met meldingen, maar **alleen van de ING-app** (`com.ing.mobile`), en alleen als Odelia het bij Geld aanzet (staat standaard uit) én in Android "Meldingen lezen" toestaat (`meldingen/BankLuisteraar.kt`, regels in `logica/BankMelding.kt`, kaarten in `ui/geld/GeldBank.kt`). Bekende winkel → vanzelf in het potje. Onbekende winkel → melding met 3 knoppen (meer kan Android niet; de rest kiest ze in de app bij Geld) en de keuze wordt onthouden (eerste 2 woorden van de naam, zonder cijfers). Bijschrijving → potje Inkomsten; ze kiest alleen het soort (Loon, Terug …). Boekingen uit een melding hebben "🔔" in de notitie. Aan/uit, onthouden winkels, wachtrij en niet-begrepen teksten staan in `bank.json` (`noBackupFilesDir`): alleen op de telefoon, **niet** in de sync en niet in de back-up. Alleen de boeking zelf komt in `geld.json` (en dus in de sync). Geen nieuwe toestemming in het manifest (de dienst is afgeschermd met `BIND_NOTIFICATION_LISTENER_SERVICE`); CI controleert dat er maar één meelezer is en dat die alleen ING leest. Google Pay-meldingen worden **niet** gelezen (anders telt een betaling dubbel). **De echte tekst van ING-meldingen is nog niet gezien:** de herkenning is ruim gebouwd op verzonnen voorbeelden; wat hij niet snapt staat bij "❓ Niet begrepen" op de kaart. Vraag Odelia om die teksten en pas `BankMelding` (en de testjes) dan aan. Omdat de app als APK is geïnstalleerd, moet ze misschien eerst "Beperkte instellingen toestaan" (staat als uitleg op de kaart).
- **Wensen van Odelia, nog te bouwen (nov 2026), in haar volgorde:** (1) uren: logboek om terug te kijken, statistieken dag/week/maand/jaar, weekstreep (5 bolletjes, oranje = geschat), vrij/ziek met één tik, weekcheck vóór de urenmail, uren bij Geld (uurloon zelf invullen, alleen op telefoon) + maandcheck met loon; knop Werk-wifi (inlogpagina openen, naam/e-mail invullen, **geen wachtwoord bewaren**; eerst schermfoto van de pagina). (2) Geld: inkomsten per maand + grafieken, totaal in een rij, minimum per pot en rest verdelen. (3) Onthoud: hartjes al invullen vóór het opslaan; foto van to-do-lijstje → regels (tekst lezen op de telefoon, nog niet geprobeerd). (4) Schema-tekenbord in de Simulator: vakken Veld → Junction box → MCC (geen controlekamer), symbolen-collectie slepen, eigen bibliotheek, schematisch/realistisch, opdrachtjes, eenlijnig schema, niveau 3 instrumentatie (loop-schema's, 4-20 mA, P&ID). Werk-tekeningen nooit letterlijk in de openbare code. (5) Website voor de laptop (alleen ☁️-onderdelen).

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
- Kompas stap 2–4 (nog te doen): Papieren. (Nu doen-kaart is gebouwd, zie hieronder.)
- Sync stap 3: Onthoud (+Mensen), Wensen, Leren, Recepten, Instellingen via Supabase. ☁️-kaart ook bij Instellingen. Supabase-SQL moet één keer opnieuw (`instellingen` in de check). **Nog niet getest met echte Supabase en niet op de telefoon.**
- Beleggen: gebouwd op tak `claude/beleggen-menu` (bovenop `claude/menu-groepen`, nov 2026). GitHub bouwt de app, 13 testjes slagen. **Nog niet getest op de telefoon; koers ophalen bij Yahoo nog niet echt geprobeerd.**
- Uitklok-vangnet: gebouwd op tak `claude/uitklok-vangnet` (bovenop `claude/beleggen-menu`), versie 0.5. **Nog niet getest op de telefoon.**
- Vertalen: gebouwd op tak `claude/vertalen-widget` (bovenop `claude/uitklok-vangnet`), versie 0.6. GitHub bouwt de app en de testjes slagen. **Nog niet getest op de telefoon; of de Claude-app de gedeelde tekst goed aanneemt is nog niet geprobeerd.**
- Urenlogboek (deel van wens 1): knop op de Werk-tab → `ui/werk/LogboekScherm.kt` + `logica/UrenStats.kt`: uren per week/maand/jaar met gemiddelde, per maand alle dagen terugkijken en aanpassen. Werktijden worden nu 400 dagen terug opgehaald. Zelfde tak, versie 0.7. **Nog niet getest op de telefoon.** Nog niet: weekstreep, vrij/ziek met één tik, weekcheck, uren bij Geld, Werk-wifi.
- Schema lezen (eerste stap van wens 4): tabje "📐 Schema lezen" in de Simulator (`ui/leren/LoopDeel.kt`, `logica/LoopSchema.kt`). Oefen-lus van een afsluiter als Veld → Junction box → MCC: meldingen open/dicht via scheidingsversterker naar ingangen, aansturing via magneetventiel. Open/dicht sturen, storing erin zetten (draad los, geen lucht), tik op blokje = uitleg, 3 opdrachtjes. Opbouw naar een voorbeeld van Odelia's werk, **alle namen en nummers verzonnen**. Zelfde tak, versie 0.8. **Nog niet getest op de telefoon.**
- Vertalen via backend (widget 🎤 → Claude → zwart balkje): zelfde tak, versie 0.9. Backend-testjes slagen (7). Nov 2026 (gekozen door Odelia): model is nu Haiku (`claude-haiku-5-5`, goedkoper; zonder effort-instelling, want niet nagekeken of Haiku die kent) en er is een daglimiet van 100 vertalingen (teller in Cloudflare KV, binding `TELLER`, wrangler maakt hem zelf aan bij de eerste deploy; alleen het aantal per dag wordt bewaard, geen tekst). 11 testjes slagen. **Niet geprobeerd met de echte Claude-API of echte KV.** **Backend nog niet online gezet (Cloudflare-account en Anthropic-sleutel nodig), niet getest op de telefoon.**
- Weekstreep + vrij/ziek met één tik (nov 2026): op het tabje Vandaag onder de klok-kaart. Vijf bolletjes ma–vr (`ui/thuis/WeekstreepKaart.kt`, `logica/Weekstreep.kt`, 4 testjes). Knoppen "🌴 Vrij vandaag" / "🤒 Ziek vandaag" alleen op een lege werkdag; daarna "↩️ Toch niet". Zelfde tak, versie 0.10. GitHub bouwt de app en de testjes slagen. **Niet getest op de telefoon.**
- Simulator (nov 2026, versie 0.11): knop "❓ Uitleg: hoe werkt dit?" onder de tabjes, met per tabje 3–5 korte stappen en voorlezen (`logica/SimulatorUitleg.kt`, 3 testjes). Tabjes van de Simulator, vak-keuze bij Toetsen en onderwerpen bij Leren lopen nu door op een nieuwe regel (FlowRow) in plaats van opzij scrollen. **Niet getest op de telefoon.** Het tekenvlak zelf (`assets/schakelbord.html`) is niet veranderd: het rijtje onderdelen scrolt daar nog opzij, omdat je er onderdelen uit sleept. Wat er precies "te breed" was, is niet zeker: vraag Odelia om een schermfoto.
- In `ui/leren/` zit geen sleep-onderdeel in de kaarten of quiz (gezocht op drag/swipe/pager, nov 2026). Slepen zit alleen in het tekenvlak. Vraag Odelia welke oefening ze bedoelt met "slepen".
- Uren bij Geld + maandcheck (nov 2026, versie 0.12): kaart "⏱️ Uren en loon" onderaan het Geld-scherm (`ui/geld/GeldUren.kt`, `logica/MaandCheck.kt`, 5 testjes). Per maand: uren en dagen (uit `UrenStats`), uren × uurloon naast het loon dat die maand bij Geld is geboekt (inkomen met categorie "Loon"); binnen 5% = "klopt ongeveer". Uurloon vult Odelia zelf in; het staat in een eigen bestand `uurloon.json` (`noBackupFilesDir`), **niet** in de sync, niet in Instellingen en niet in de back-up. Sync-lijst niet veranderd. Het is een schatting (bruto/netto, loon komt soms later). **Nog niet getest op de telefoon.**
- Nu doen-kaart (Kompas, nov 2026, versie 0.13): op het tabje Vandaag onder de klok-kaart (`ui/thuis/NuDoenKaart.kt`, `logica/NuDoen.kt`, 3 testjes). Laat één ding zien uit Onthoud: eerst wat te laat is (oudste eerst, koraal), anders wat vandaag moet (geel). Niks voor nu = geen kaart. Knoppen: 🔊 voorlezen, "✅ Klaar"; tik op de kaart opent de Onthoud-lijst. Gebruikt `Overzicht`. Zelf gekozen: onder de klok-kaart, zodat de in/uit-knop bovenaan blijft. **Nog niet getest op de telefoon.**
- Geld uit ING-meldingen (nov 2026, versie 0.14): tak `claude/geld-uit-meldingen` (bovenop `claude/vertalen-widget`). 9 testjes voor de herkenning en de regels slagen; de schermen compileren in de voorproef. Het Android-deel (meelezer, melding met knoppen) kan alleen GitHub bouwen. **Niet getest op de telefoon en nog nooit een echte ING-melding gezien.**
- Opmaak (21 nov 2026, versie 0.15, tak `claude/opmaak-wensen-geld-sim`, bovenop `claude/geld-uit-meldingen` + `claude/vertalen-widget`, met de 5 nieuwe wensen van main): Wensen krijgen een **beschrijving** (`Wens.beschrijving`, in JSON/sync), alleen te zien in een pop-up als je op de tegel tikt (daar ook ✏️ Aanpassen). Knop "Nieuwe wens" staat op een eigen regel. Wens-tegels en potjes-tegels staan in 2 kolommen zonder gaten (`Wensen.tweeKolommen`, kortste kolom eerst). Geld: "Totaal" en bedrag op één regel, bedragen in de potjes rechts uitgelijnd, alle 🔊-knoppen bij Geld weg (gekozen door Odelia). Tekenvlak: in Bedien 1× tikken op schakelaar/contact = open/dicht zonder kaart, 2× tikken = kaart ernaast (eerste tik wordt teruggedraaid). Namen (K1, Q1…) naast het onderdeel: boven als de draden horizontaal lopen, links als er rechts een draad loopt. Compileert in de voorproef; testjes konden in de cloud niet draaien (Google Maven geblokkeerd). **Niet getest op de telefoon.**
- **Bouwnacht 1 (21 nov 2026, versie 0.16, tak `claude/bouwnacht-1`, bovenop `claude/opmaak-wensen-geld-sim`)**, gebouwd terwijl Odelia sliep (zelf gekozen waar nodig):
  - A. Sync: Advies + Zeilweer-plek erbij (`SyncRegels`, `supabase/kompas.sql`, CI). **Supabase-SQL moet één keer opnieuw** (advies en weer in de check). Mail-hulp/Thuis: niets te synchroniseren.
  - B. Versleutelde sync + geheime zin + Privacy-overzicht (zie Gemaakte keuzes).
  - C. Melding **alleen** als een potje net onder nul gaat, met tip uit welk potje (liefst niet Investeren/Noodpot/Inkomsten) (`logica/PotjeAlarm.kt`, kanaal "Geld: potje onder nul"). Logboek "📜 Laatst gebeurd" bij Instellingen: sync, ING-boeking, in/uitgeklokt (`logica/Logboek.kt`, `logboek.json` in `noBackupFilesDir`, max 30). Kaart "🤖 Claude-kosten deze maand" bij Instellingen: bedrag **zelf invullen** uit de Anthropic Console (de app kan het niet zien) + limiet (standaard € 5), waarschuwing vanaf 80% (`logica/ApiKosten.kt`). Urenmail-seintje op vrijdag komt nu ook als ze die dag niet in/uitklokte (vrij, ziek, vergeten), om uitkloktijd + 30 min (`Planner`, code 8). Wensen: hartjes al kiezen in het venster vóór bewaren. Lijstje: gedeelde tekst met 2+ regels → knop "Maak er N losse dingen van" in Onthoud (`logica/Lijstje.kt`). **Foto → tekst** gaat via Google Lens (tekst kiezen → delen met Bash-bord), niet in de app: ML Kit stuurt gebruiksgegevens naar Google en voegt toestemmingen toe. "Minimum per pot, rest verdelen" en "totaal in één rij" zaten er al in.
  - **Niets hiervan getest op de telefoon of met echte Supabase.**
- **Bouwnacht 2 (22 nov 2026, versie 0.17, tak `claude/bouwnacht-2`, bovenop `claude/bouwnacht-1`)**, gebouwd terwijl Odelia sliep:
  - A. Grafieken (`ui/onderdelen/Staafjes.kt`, `logica/Vooruit.kt`): uren per dag (laatste 10 werkdagen) en per maand (6) in het Urenlogboek; inkomsten en uitgaven per maand (6) bij Geld.
  - B. Maandafsluiting op Thuis (`ui/thuis/MaandAfsluitingKaart.kt`): de eerste 7 dagen van een maand (voor de vorige maand) en de laatste 2 dagen. Erbij/uit, saldo per potje, en de rest uit Vrije geld (anders Inkomsten) → knoppen 🐷 Sparen / 📈 Beleggen / Laten staan (een "Over"-boeking). Daarna weg voor die maand (`GeldData.afgesloten`, in de sync).
  - C. Vaste lasten vooruit (`ui/geld/GeldVooruit.kt`): zelf toevoegen (naam, bedrag, dag 1–28, potje; `GeldData.vasteLasten`, in de sync). Lijst van de komende 31 dagen en een ⚠️-waarschuwing als een potje ze niet kan betalen, bij Geld én op Thuis.
  - D. Wensen bij een potje: prijs + potje in het wens-venster (`Wens.prijsCent`, `Wens.potjeId`); in de pop-up "€ x van € y, genoeg rond <maand>" (gemiddelde van wat er de laatste 3 maanden bij kwam).
  - E. Uitklok-melding met zoempje + geluid; poort-check (zie Gemaakte keuzes).
  - G. Nieuws op Thuis (alleen 's ochtends, vóór 12:00): koppen van de NOS (`feeds.nos.nl`, publieke omroep, feitelijk), pas na tik op "Ja, ophalen". Toegevoegd aan `Online.kt` en de CI-lijst. Voorlezen kan. **Niet geprobeerd of de NOS-feed doorverwijst** (de app volgt geen doorverwijzingen; dan "Ophalen lukte niet").
  - **F. Weekplanning via foto + Google Agenda + energie-check: nog niet gebouwd** (te groot voor deze nacht; Agenda lezen heeft een nieuwe toestemming nodig). Voorstel voor later: lijstje delen (Google Lens) → taken met duur en plek → vrije blokken uit de agenda van de telefoon (`CalendarContract`, alleen lezen) → na de week "klopte het?" met 3 smileys.
  - **Niets hiervan getest op de telefoon.**
- **Inloggen met de link uit de mail (22 nov 2026, versie 0.18, tak `claude/inloggen-link`, bovenop `claude/bouwnacht-2`):** nieuw Supabase-project (`jcwatraewexvozoaefnw`, org "kompass", login met GitHub; het oude account was kwijt en de sync had nog nooit online gestaan). Nieuwe projecten sturen alleen een link, geen code, en de mail aanpassen kan pas met eigen SMTP. Daarom: `vraagCode` stuurt `redirect_to=bashbord://login`; Supabase stuurt na de tik door naar `bashbord://login#…refresh_token=…`; intent-filter op `MainActivity` → `Sync.logInMetLink` → `logica/LoginLink.kt` (3 testjes) → sessie via refresh-token. Alleen het gevraagde adres, en niet een ander account dan waarop je al zit. Code typen blijft als reserve. In Supabase gezet (door Claude): Site URL + Redirect URL `bashbord://login`; tabel `kompas_data` met de bouwnacht-2-SQL. GitHub-geheimen `SUPABASE_URL` en `SUPABASE_ANON_KEY` door Odelia gezet. **Niet getest op de telefoon; niet geprobeerd of Gmail/Chrome de bashbord://-link zonder vraag doorgeeft.** Daarna: "Allow new users to sign up" uit.
- **Versie 0.19 (22 nov 2026, zelfde tak):** knop "↩️ Nieuwe mail" op het scherm "Kijk in je mail" (je zat daar vast), en bij een verlopen/foute link gaat de app meteen terug naar het e-mailvakje. Oorzaak "verlopen of al gebruikt": meestal een oudere mail of link 2x geopend.
- Nog te doen van Odelia's lijst (nov 2026): Werk-wifi-knop (wacht op een foto van de loginpagina van Odelia).
- Let op: `claude/menu-groepen` en `ccr-4151dad2-s3u63m` (urenmail, Google-spreadsheet) zijn nog niet samengevoegd.
- Nog te doen: sync voor foto's; tegels op de tablet bespreken; webpagina voor de laptop; Google Agenda; Gmail-koppeling (concept in de draad), telefoon op stil bij inklokken (fase 2-wens).
