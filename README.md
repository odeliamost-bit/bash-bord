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

## ☁️ Sync tussen apparaten (één keer instellen)

Dan staan Geld, Onthoud (en Mensen), Wensen, Leren, Recepten en Instellingen op je telefoon, tablet en laptop tegelijk.
Grote tekst en tegels blijven per apparaat. Foto's gaan nog niet mee.

> **Had je Supabase al ingesteld vóór november 2026?** Plak `supabase/kompas.sql` dan nog één keer en tik **Run** (stap 1). Anders gaan de Instellingen niet mee.
Uren, Cyclus, Mail-hulp, Advies, Weer en Thuis gaan nooit via Supabase. (Uren kunnen wel naar je Google-spreadsheet, zie hieronder.)

**1. Tabel maken in Supabase**
- Open je project op supabase.com ➜ **SQL Editor** ➜ **New query**.
- Plak alles uit `supabase/kompas.sql` en tik **Run**.

**2. Inloggen met een code aanzetten**
- **Authentication ➜ Sign In / Providers ➜ Email**: aan.
- **Authentication ➜ Emails**: zet bij **Magic Link** én **Confirm signup** de code in de mail: `{{ .Token }}`.

**3. Twee geheimen in GitHub**
- Supabase ➜ **Project Settings ➜ API**: kopieer de **Project URL** en de **anon/publishable key**.
- GitHub ➜ **Settings ➜ Secrets and variables ➜ Actions ➜ New repository secret**:
  - `SUPABASE_URL` = de Project URL (begint met `https://`)
  - `SUPABASE_ANON_KEY` = de anon/publishable key
- Nooit de **service_role/secret key** gebruiken.

**4. Inloggen in de app**
- Eerst op de **telefoon**: Instellingen (of Geld) ➜ ☁️-kaart ➜ e-mail ➜ code uit de mail.
- Daarna op de tablet. Let op: de eerste keer wint wat online staat. (Wat er op de tablet stond, blijft als reserve bewaard.)

**5. Alleen jij**
- Na je eerste inlog: **Authentication ➜ Sign In / Providers** ➜ **Allow new users to sign up** uit.

## 📊 Uren naar Google Sheet (één keer instellen)

Na het uitklokken zet de app begin, eind en pauze in "Cargill_Registratie Werktijden_Odelia". Alleen lege dagen.
Stappen: zie [`hulpmiddelen/sheet/README.md`](hulpmiddelen/sheet/README.md). Geheimen: `SHEET_URL` en `SHEET_TOKEN`.

## 🛡️ Beveiliging

**Wat de app al doet**
- 🌐 De app gaat **alleen online als jij eerst "ja" zegt**, en alleen voor **Zeilweer**: dan haalt hij het weer op bij Open-Meteo (gratis weerdienst). Alleen je zeilplek gaat mee. Je uren en afspraken gaan **nooit** het internet op.
- 🔎 **Opzoeken** en 🗺️ **route**: na jouw "ja" gaan Chrome of Google Maps open. De app zelf gaat dan niet online.
- 🧱 Alle internet-code staat in **één bestand** (`online/Online.kt`): alleen versleuteld (https), alleen naar Open-Meteo, nooit meer dan 2 MB. GitHub controleert dit bij elke versie, en ook dat de app geen andere toestemmingen vraagt (zoals locatie, camera of contacten).
- 🔒 Je krijgt de **echte versie**, geen testversie. Met een kabel kan niemand in de app kijken.
- 🚪 Andere apps kunnen de app niet stiekem laten klokken of meldingen laten sturen.
- 🧾 Bij elke versie staat een **controlecode** (SHA-256) op de downloadpagina.

**Wat het bouw-recept doet**
- 📌 Alle hulpjes staan vast op een precieze versie.
- 🔑 Elk onderdeel heeft zo weinig rechten als mogelijk.
- ✅ Het bouwgereedschap (Gradle) wordt gecontroleerd met een controlecode.
- 🤖 **Dependabot** maakt elke zaterdag een voorstel als er een update is.

**Wat jullie zelf kunnen doen (aanrader)**
1. 🔐 **Twee-stapsverificatie** aan op GitHub: *Settings › Password and authentication*.
2. 🙈 Repository **privé** maken: *Settings › General › Danger Zone › Change visibility*.
3. 🔔 **Dependabot-meldingen** aan: *Settings › Advanced Security* (of *Code security*) › *Dependabot alerts* en *Dependabot security updates*.
4. 📱 Na het installeren **"onbekende apps toestaan" weer uitzetten** voor je browser.
5. 📱 Een **schermvergrendeling** op je telefoon (pincode, patroon of vingerafdruk). Dan zijn je gegevens versleuteld.

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
| `app/src/main/java/.../ui/leren/` | Leren: kaarten, quiz, zelf kaarten maken, pauze |
| `app/src/main/java/.../ui/menu/` | Zijbalk (menu) |
| `app/src/main/java/.../ui/onderdelen/` | Bouwsteentjes: klei-kaarten, knoppen, icoontjes |
| `app/src/main/java/.../meldingen/` | Herinneringen en meldingen |
| `app/src/main/java/.../widget/` | Widgets op het startscherm |
| `app/src/test/` | Testjes voor de rekenregels |
| `ontwerp/` | Het goedgekeurde ontwerp |
| `hulpmiddelen/voorproef/` | Hulpje voor Claude om te checken zonder telefoon |
| `licenties/` | Licentie van het lettertype Nunito (vrij te gebruiken) |
