# 📒 Logboek

Wat Claude zelfstandig gebouwd heeft, per stap.
Per stap: ✅ wat klaar is · 🖼️ plaatjes · 🔍 wat getest is (en wat niet) · ❓ vragen en keuzes voor Odelia.

De plaatjes staan in de map `logboek/`. Ze komen uit het voorproef-hulpje (computer-versie van de schermen), **niet** van een echte telefoon.

---

## 0. Feedback op stap 1 (24 okt)

✅ **Klaar**
- 📋 Zijbalk: tik op een icoontje = meteen erheen. Alleen het oranje knopje maakt hem breed.
- ✨ Tik-effect: geen vlek meer. Knop wordt heel even iets kleiner.
- 🎨 Icoontjes: Geld = munt met €. "Komt later" = rond klei-knopje. 9 scheve icoontjes rechtgezet.
- 🗑️ Wachtwoorden weg.

🔍 **Getest**
- ✅ Compileert (voorproef met Compose 1.7.3).
- ✅ **Echte Android-app gebouwd in de cloud** (`app-debug.apk`). Dat lukte nu voor het eerst: `dl.google.com` werkt sinds vandaag.
- ❌ Niet op een telefoon getest. Een emulator kan hier niet draaien.

❓ **Voor jou**
- Niets.

🖼️ **Plaatjes** (links voor, rechts na)

![Menu voor en na](logboek/0-menu-voor-na.png)
![Komt later voor en na](logboek/0-komt-later-voor-na.png)
![Tegels voor en na](logboek/0-tegels-voor-na.png)

---

## 2. ⏱️ In- en uitklokken

### 2a + 2b: echt klokken, overzicht, dag aanpassen

✅ **Klaar**
- 💾 De knop bewaart nu echt. Opslag met Room, alleen op de telefoon. Elke regel heeft een UUID en `updated_at` (klaar voor later synchroniseren).
- 🟢 Groen = aan het werk: "sinds 7:45" en "al 5½ uur gewerkt" (loopt mee, elke 30 sec).
- 🌸 Roze = klaar: "Klaar voor vandaag", uren na de pauze, "uitgeklokt om 15:50".
- 🧮 Pauze: altijd 45 min eraf (nooit onder 0). Twee keer klokken op één dag mag; de pauze gaat er maar één keer af.
- 📊 Tegels Vandaag/Deze week en het Werk-tabje (ring + staafjes) tonen echte uren.
- ✏️ Tik op een dag (tegel, legenda of staafje) → dag aanpassen:
  - 💼 gewerkt / 🌿 vrij / 🤒 ziek
  - begin- en eindtijd met knoppen −1 u, −5, +5, +1 u
  - "Nog een blok" en "Blok weghalen" (twee keer tikken, zodat het niet per ongeluk gaat)
- ⏰ Vergeten uit te klokken? Dan verschijnt een geel kaartje met de knop "om 15:45" of "andere tijd".
- 🎯 Weekdoel: tik op "doel 35 u" in het Werk-tabje en pas het aan met − en +.

🖼️ **Plaatjes**

| Aan het werk | Klaar | Vergeten |
|---|---|---|
| ![](logboek/2-vandaag-aan-het-werk.png) | ![](logboek/2-klaar-voor-vandaag.png) | ![](logboek/2-vergeten-uitklokken.png) |

| Werk-tabje | Dag aanpassen |
|---|---|
| ![](logboek/2-werk-week.png) | ![](logboek/2-dag-aanpassen.png) |

🔍 **Getest**
- ✅ Echte Android-app gebouwd.
- ✅ 9 tests voor de urenberekening (pauze, twee blokken, vrij/ziek, vergeten, weekend) slagen.
- ✅ Plaatjes bekeken en vergeleken met het ontwerp.
- ❌ Niet op de telefoon getest: opslaan, de klok die meeloopt en het knoppen-gevoel.
- ⚠️ Emoji (💼 🌿 🤒) zien er op de plaatjes zwart-wit uit. Op de telefoon zijn ze in kleur.

❓ **Keuzes die ik gemaakt heb** (zeg het als je het anders wilt)
- 🔢 Tijdens het werk: "Vandaag" = tijd sinds inklokken (zoals in het ontwerp). Na het uitklokken: uren mét de pauze eraf.
- 🌿 Vrije en zieke dagen tellen als 0 uur. Ze tellen niet mee voor je 35 uur. **Klopt dat?**
- ⏰ Bij "vergeten uit te klokken" stelt de app 15:45 voor.

### 2c: widget op het startscherm 🏠

✅ **Klaar**
- Widget "Klokken" (4 × 2 vakjes), zoals in het ontwerp:
  - links de grote knop: groen = aan het werk, roze = niet. **Tik = in- of uitklokken.**
  - rechts de ring met je uren van deze week. Tik = app openen.
- De widget ververst meteen na elke klik (in de app, in de widget of in een melding) en verder elk half uur.
- 📲 Zo zet je hem erop: lang drukken op het startscherm › Widgets › "Mijn dashboard" › "Klokken".

🖼️ **Plaatjes** (getekend met Robolectric, geen echte telefoon)

![Widget](logboek/2-widget.png)

🔍 **Getest**
- ✅ Gebouwd, en 3 plaatjes gemaakt van de widget (aan het werk, nog niet gestart, klaar).
- ❌ Niet op de telefoon getest: of de klik werkt en hoe hij er in jouw launcher uitziet.
- ⚠️ Op de plaatjes zijn de hoekjes niet rond. Op Android 12+ zijn ze dat wel, maar dat kan Robolectric niet tekenen.
- ⚠️ Een widget kan geen eigen lettertype gebruiken. De tekst is dus de gewone telefoonletter, niet Nunito. "UIT"/"IN" op de knop is wel Nunito, want dat is een plaatje.
- ⚠️ "al 5½ uur gewerkt" in de widget kan tot een half uur achterlopen. Android laat een widget niet vaker verversen.

### 2d: herinneringen 🔔

✅ **Klaar**
- ma–vr **8:15**: nog niet ingeklokt? → melding met knoppen **Inklokken · Vandaag vrij · Ziek**.
- ma–vr **16:15**: nog ingeklokt? → melding met knoppen **Uitklokken · Over 10 min · Over 20 min** (jouw snooze-wens).
- Op een vrije of zieke dag komt er geen inklok-melding.
- Klok je in de app in of uit, dan verdwijnt een melding die niet meer klopt vanzelf.
- Na het opnieuw opstarten van de telefoon zet de app de herinneringen weer klaar.
- De eerste keer vraagt Android of de app meldingen mag sturen. Zeg **"Toestaan"**.

🔍 **Getest**
- ✅ 4 tests voor de planning, bijvoorbeeld vrijdagavond → maandag 8:15 en weekend → maandag.
- ✅ 3 Robolectric-tests: de melding heeft de 3 knoppen, zonder toestemming komt er geen melding, en de wekkers staan op 8:15 en 16:15.
- ✅ Android-lint: 0 fouten.
- ❌ Niet op de telefoon getest.
- ⚠️ Android mag een herinnering een paar minuten later laten komen, om de batterij te sparen. Precies op de minuut kan alleen met een extra toestemming ("Wekkers"). Die heb ik expres niet gevraagd.

❓ **Voor jou**
- 🔕 Komen de meldingen niet? Kijk dan bij Instellingen › Apps › Mijn dashboard › Meldingen. Op de Nothing Phone ook bij Batterij › "Niet beperken".
