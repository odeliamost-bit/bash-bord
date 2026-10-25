# 📚 Plan: het Leren-scherm

Dit is een plan. Er staat nog **geen code** in. Lees eerst `CLAUDE.md`. Alle regels daar blijven gelden: eerst een plan, dan "mag ik?", en daarna kleine stappen.

## ✅ Al gekozen (25 oktober 2026)

- **Eerst fase 1 afmaken.** Leren komt daarna, in fase 2. Begin hier pas aan als Odelia dat zegt.
- **Sleutel voor Claude: via een backend** (server). Zie 🔑.

## 🎯 Wat Odelia wil

Ze volgt een MBO-opleiding elektrotechniek. In het Leren-scherm wil ze:

- 🃏 **Overhoren** met flashcards en een herhaalsysteem.
- 💡 **Leg uit**: een tutor (Claude) die uitlegt in kleine stappen.
- ✨ **Kaarten maken**: een onderwerp of een stuk lesboek erin, kaarten eruit.

Er bestaat al een werkend voorbeeld als webpagina: `leren-prototype.html`. Daarin kun je zien hoe het werkt en welke teksten en prompts erin staan. Het uiterlijk komt **niet** uit dat voorbeeld, maar uit `ontwerp/Leren.dc.html`.

## 🗂️ Bestanden hier

| Bestand | Wat |
|---|---|
| `leren-kaarten.json` | 44 basiskaarten in 6 onderwerpen. Later naar `app/src/main/assets/`. |
| `leren-prototype.html` | Werkend voorbeeld. Gebruik het alleen voor gedrag, teksten en prompts. |

## 🧩 Past in het ontwerp

`ontwerp/Leren.dc.html` heeft al deze onderdelen:

| In het ontwerp | Wordt |
|---|---|
| ❓ Overhoren "stel me vragen" | Flashcards (§1) |
| 💡 Leg uit "ik snap iets niet" | Tutor (§2) |
| 🎯 Vandaag "x van y klaar" | Eén doel is bijvoorbeeld "10 kaarten oefenen" |
| ⭐ Deze week geleerd | Ook het aantal geoefende kaarten per dag |
| 📖 Mijn schoolboek, Focus-tijd | Blijven zoals in het ontwerp |

Voeg **"Kaarten maken"** toe in dezelfde klei-stijl. Bijvoorbeeld als derde knop naast Leg uit en Overhoren, of in het Overhoren-scherm zelf. Vraag Odelia wat ze fijner vindt.

## 1️⃣ Flashcards (werkt zonder internet)

- **Groepenkast**: 5 vakjes (G1 t/m G5) met het aantal kaarten. G1 = "vaak herhalen", G5 = "zit erin".
- **Onderwerp kiezen**: Alles, Basis, Vermogen, Schakelingen, Wisselstroom, Installatie, Componenten, Eigen kaarten.
- **Rondje**: maximaal 10 kaarten die klaar zijn. Eerst de kaarten die ze al eens zag (laagste groep eerst), daarna de nieuwe.
- **Eén kaart tegelijk**: vraag → "Toon antwoord" → antwoord en uitleg → drie grote knoppen.
- 🔊 **Voorlezen** bij vraag en antwoord, met de bestaande `Voorlezer.kt`.
- Een knop **"Leg dit uit"** opent de tutor met deze kaart.
- **Na het rondje** zie je hoeveel "Wist ik", "Twijfel" en "Nog niet". Daaronder de knop "Nog een rondje".

**Herhaalregels**: neem ze precies zo over.

| Knop | Groep | Komt terug |
|---|---|---|
| Wist ik niet | wordt 1 | nu; nog 1× aan het eind van dit rondje |
| Twijfel | blijft gelijk | over 1 dag |
| Wist ik | +1 (max 5) | G1: nu · G2: 1 dag · G3: 3 dagen · G4: 7 dagen · G5: 14 dagen |

Een nieuwe kaart begint in G1 en is meteen klaar.

## 2️⃣ Tutor (Leg uit)

- Een chat met snelknoppen: "Overhoor mij", "Help met een som", "Leg de wet van Ohm uit".
- Het antwoord verschijnt terwijl het geschreven wordt. Er is een Stop-knop en een 🔊-knop.
- Er is ook een **inspreek-knop** 🎤, want praten kost Odelia minder energie dan typen.
- De vaste instructie voor Claude staat letterlijk in `leren-prototype.html`, in de functie `rules()`. Neem die over en vul het niveau in.

## 3️⃣ Kaarten maken

- Een tekstvak "Onderwerp, of plak tekst uit je les", plus een **foto-knop** 📷 voor een bladzijde uit het lesboek.
- Claude maakt 8 kaarten. Odelia ziet ze eerst, kan losse kaarten weghalen, en kiest dan "Bewaar" of "Niet bewaren".
- De prompt staat in `leren-prototype.html`, in de functie `generate()`. Het antwoord is JSON: `{"onderwerp": "...", "kaarten": [{"q","a","x"}]}`.
- Controleer het antwoord: elke kaart heeft een niet-lege vraag en een niet-leeg antwoord, en er zijn maximaal 12 kaarten. Is het antwoord kapot? Laat dan een nette melding zien.

## 🔑 Belangrijk: Claude in de app

De tutor en "kaarten maken" hebben de Claude API nodig. **Er mag geen API-sleutel in git** (regel 5).

Odelia koos voor een **backend**. Bijvoorbeeld een Cloudflare Worker, die staat al bij "later" in `CLAUDE.md`. De sleutel staat dan veilig op de server, en de app praat alleen met de server.

- Maak eerst een kort plan voor die backend: wat er nodig is, en wat het kost.
- Vraag daarna of je mag beginnen.

Tot de backend er is werken de flashcards al wel, en kan Odelia de tutor gebruiken in de webversie.

## 💾 Data (Room)

Elk record heeft een UUID en `updated_at`, zoals in `CLAUDE.md` staat.

- `LeerKaart`: id, onderwerp, vraag, antwoord, uitleg, bron (`basis`/`eigen`), `updated_at`. Basiskaarten komen uit de JSON en houden hun vaste id (`b01`, `v01` …). **Verander die id's nooit.**
- `LeerVoortgang`: kaartId, groep (1–5), volgendeKeer, `updated_at`.
- `LeerDag`: datum, aantal geoefend (voor ⭐ Deze week).
- Instelling: niveau (MBO 2, 3 of 4; standaard 3).
- Het gesprek met de tutor alleen in het geheugen, of de laatste 30 berichten lokaal.

## 🪜 Stappen (elk apart testen op de telefoon)

1. **Flashcards** met de basiskaarten, de groepenkast en de herhaalregels. Werkt helemaal offline.
2. **Koppelen** aan het Leren-scherm: de Overhoren-knop, het doel bij "Vandaag" en "Deze week geleerd".
3. **Backend voor Claude** (zie 🔑). Daarna de tutor.
4. **Kaarten maken**, eerst met tekst en daarna met een foto.

## ✅ Testen

- **Herhaalregels**: elke knop in elke groep, inclusief de grens bij G5 en het terugzetten naar G1.
- **Rondje**: maximaal 10 kaarten, de goede volgorde, en "Wist ik niet" komt precies één keer terug.
- **Kaarten maken**: lege of kapotte JSON geeft een nette melding.
- **Zonder internet**: flashcards werken; de tutor zegt "De tutor werkt alleen met internet."
