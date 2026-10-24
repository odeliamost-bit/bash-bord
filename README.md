# bash-bord

Mijn persoonlijke dashboard-app (Android).

## 📱 App op de telefoon zetten

Voor wie de laptop bedient. Eerst `EERST-DOEN.md` (Android Studio installeren, telefoon klaarzetten).

1. **Code ophalen.** In Android Studio: *File › New › Project from Version Control*.
   Plak de link van deze GitHub-repository. Kies daarna rechtsonder de juiste branch.
2. **Wachten.** Onderaan staat "Gradle sync". De eerste keer duurt dat lang (downloaden).
   - Vraagt hij iets te installeren, zoals "SDK Platform 35"? Klik op de blauwe link.
   - Vraagt hij om de "Android Gradle Plugin" te upgraden? Mag, hoeft niet.
3. **Telefoon** met de kabel aan de laptop. Op de telefoon: "Toestaan".
4. Bovenin: kies de telefoon. Druk op **▶️ Run 'app'**.
5. De app heet **Mijn dashboard** (koraal huisje). 🏠

Fout? Kopieer de rode foutmelding en plak hem bij Claude.

## 📦 APK van Claude installeren (zonder laptop)

Claude kan in de cloud een APK maken. Die kun je zo op de telefoon zetten:

1. Download het `.apk`-bestand op de telefoon (bijvoorbeeld uit de Claude-app).
2. Tik erop. Android vraagt of je apps uit deze bron mag installeren: **Toestaan**.
3. Tik **Installeren**.

⚠️ Staat de app er al op via Android Studio? Dan krijg je "App niet geïnstalleerd".
Dat komt doordat elke APK een eigen sleutel heeft. Verwijder dan eerst de oude app.
Let op: daarmee gaan de gegevens in de app verloren (uren, afspraken).
Zolang je de app echt gebruikt: kies één manier (Android Studio óf de APK van Claude) en blijf daarbij.

## 🧩 Widgets

Lang drukken op het startscherm › **Widgets** › **Mijn dashboard**:
- **Klokken**: grote in/uit-knop en je uren van deze week.
- **Onthoud**: snel iets onthouden, typen of 🎤 inspreken.

## 🗂️ Wat zit waar

| Map | Wat |
|---|---|
| `app/src/main/java/.../ui/thuis/` | Hoofdscherm: welkomkaart, tabjes, tegels |
| `app/src/main/java/.../ui/werk/` | Dag aanpassen, urenmail |
| `app/src/main/java/.../ui/onthoud/` | Onthoud dit, lijst, mensen |
| `app/src/main/java/.../ui/menu/` | Zijbalk (menu) |
| `app/src/main/java/.../ui/onderdelen/` | Bouwsteentjes: klei-kaarten, knoppen, icoontjes |
| `app/src/main/java/.../ui/theme/` | Kleuren en lettertype (Nunito) |
| `app/src/main/java/.../uren/` | Rekenregels uren, planning, urenmail |
| `app/src/main/java/.../onthoud/` | Rekenregels Onthoud: zoeken, indelen, ochtendmelding |
| `app/src/main/java/.../data/` | Opslag op de telefoon (Room) |
| `app/src/main/java/.../widget/` | Widgets op het startscherm |
| `app/src/main/java/.../meldingen/` | Herinneringen en meldingen |
| `app/src/test/` | Tests (draaien zonder telefoon) |
| `app/schemas/` | Beschrijving van de database (nodig voor latere wijzigingen) |
| `app/src/main/res/` | Plaatje, lettertype, app-icoon, widget-instellingen |
| `ontwerp/` | Het goedgekeurde ontwerp |
| `logboek/` + `LOGBOEK.md` | Wat Claude gebouwd heeft, met plaatjes en vragen |
| `GMAIL-KOPPELING.md` | Urenmail: nu, en wat later nodig is voor een echte Gmail-koppeling |
| `hulpmiddelen/voorproef/` | Hulpje voor Claude om schermen te tekenen zonder telefoon |
| `hulpmiddelen/cloud/` | Hulpje voor Claude om de echte app in de cloud te bouwen |
| `licenties/` | Licentie van het lettertype Nunito (vrij te gebruiken) |
