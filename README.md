# bash-bord

Mijn persoonlijke dashboard-app (Android).

## 📲 App op je telefoon zetten (zonder laptop)

GitHub bouwt de app vanzelf bij elke nieuwe versie.

1. Open deze link **op je telefoon**:
   https://github.com/odeliamost-bit/bash-bord/releases/download/nieuwste/mijn-dashboard.apk
2. Tik op het gedownloade bestand en daarna op **Installeren**.
3. De eerste keer vraagt de telefoon om **"onbekende apps toestaan"**. Zet dat aan voor je browser.
4. De app heet **Mijn dashboard** (koraal huisje). 🏠

Een nieuwe versie? Gewoon dezelfde link opnieuw openen.

## 🔑 Vaste handtekening (één keer instellen)

Elke app heeft een handtekening. Is die bij een nieuwe versie anders, dan wil Android hem niet bijwerken.
Dan moet je de oude app eerst verwijderen, en dan zijn je **gegevens weg** (uren, afspraken).
Daarom zet je één keer een vaste handtekening klaar. Dit doet je broer op de laptop.

1. Open in Android Studio de **Terminal** (onderaan, of *View › Tool Windows › Terminal*).
2. Typ:
   ```
   keytool -genkeypair -keystore dashboard.keystore -alias dashboard -keyalg RSA -keysize 2048 -validity 10000
   ```
   Kies een **wachtwoord** en onthoud het (bijvoorbeeld in een wachtwoordkluis). De vragen over naam mag je invullen zoals je wilt.
3. Maak er tekst van en kopieer die:
   - Windows (PowerShell): `[Convert]::ToBase64String([IO.File]::ReadAllBytes("dashboard.keystore")) | Set-Clipboard`
   - Mac: `base64 -i dashboard.keystore | pbcopy`
4. Op GitHub: deze repository › **Settings** › **Secrets and variables** › **Actions** › **New repository secret**.
   - Naam `APP_SLEUTEL`, waarde: plakken.
   - Nog een: naam `APP_SLEUTEL_WACHTWOORD`, waarde: het wachtwoord.
5. Bewaar `dashboard.keystore` veilig op de laptop. **Nooit** in git zetten.
6. Daarna: oude app **één keer** verwijderen en de nieuwe installeren. Vanaf dan gaan updates gewoon over de oude heen.

## 💻 Via Android Studio (met kabel)

Voor wie de laptop bedient. Eerst `EERST-DOEN.md` (Android Studio installeren, telefoon klaarzetten).

1. In Android Studio: *File › New › Project from Version Control* en plak de link van deze repository.
2. Wacht tot "Gradle sync" klaar is. Vraagt hij iets te installeren? Klik op de blauwe link.
3. Telefoon met de kabel aan de laptop, kies hem bovenin en druk op **▶️ Run 'app'**.

Fout? Kopieer de rode foutmelding en plak hem bij Claude.

## 🗂️ Wat zit waar

| Map | Wat |
|---|---|
| `app/src/main/java/.../logica/` | Rekenregels: uren, pauze, urenmail, herkennen van inspreken (met testjes) |
| `app/src/main/java/.../data/` | Opslag op de telefoon (Room) |
| `app/src/main/java/.../ui/thuis/` | Hoofdscherm: welkomkaart, tabjes Vandaag / Werk / Onthoud |
| `app/src/main/java/.../ui/onthoud/` | Onthoud dit, lijst met zoeken, per persoon |
| `app/src/main/java/.../ui/werk/` | Urenmail |
| `app/src/main/java/.../ui/menu/` | Zijbalk (menu) |
| `app/src/main/java/.../ui/onderdelen/` | Bouwsteentjes: klei-kaarten, knoppen, icoontjes |
| `app/src/main/java/.../meldingen/` | Herinneringen en meldingen |
| `app/src/main/java/.../widget/` | Widgets op het startscherm |
| `app/src/test/` | Testjes voor de rekenregels |
| `ontwerp/` | Het goedgekeurde ontwerp |
| `hulpmiddelen/voorproef/` | Hulpje voor Claude om te checken zonder telefoon |
| `licenties/` | Licentie van het lettertype Nunito (vrij te gebruiken) |
