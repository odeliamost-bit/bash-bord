# 📊 Uren naar Google Sheet (één keer instellen)

De app zet na het uitklokken je **begin, eind en pauze** in de spreadsheet
**"Cargill_Registratie Werktijden_Odelia"**. Alleen dagen die nog leeg zijn.
Totaal, opmerkingen en de loon-kolommen blijven van jou.

⚠️ Je uren staan dan online bij Google, in deze ene spreadsheet. De eigenaar van de spreadsheet ziet ze ook.

## 1. Script in de spreadsheet zetten 🔧
1. Open de spreadsheet op de laptop.
2. **Extensies ➜ Apps Script**.
3. Haal alles weg wat er staat. Plak alles uit `Code.gs` (deze map). Tik 💾 **Opslaan**.

## 2. Een geheime code verzinnen 🔑
1. Verzin een lange code, bijvoorbeeld 30 letters en cijfers door elkaar. (Geen echt wachtwoord gebruiken!)
2. In Apps Script: ⚙️ **Projectinstellingen ➜ Scripteigenschappen ➜ Eigenschap toevoegen**.
   - Eigenschap: `TOKEN`
   - Waarde: je code
3. Heet het tabblad onderaan niet `Sheet1`? Voeg dan ook `BLAD` toe met de naam van het tabblad.

## 3. Het script "openzetten" voor de app 🌐
1. **Implementeren ➜ Nieuwe implementatie**, type: **Web-app**.
2. Uitvoeren als: **Ik**. Wie heeft toegang: **Iedereen**.
   (Zonder de geheime code doet het script niks.)
3. Tik **Implementeren** en geef toestemming met je Google-account.
4. Kopieer de **URL van de web-app** (eindigt op `/exec`).

## 4. Twee geheimen in GitHub 🔒
GitHub ➜ **Settings ➜ Secrets and variables ➜ Actions ➜ New repository secret**:
- `SHEET_URL` = de URL van stap 3
- `SHEET_TOKEN` = de code van stap 2

Daarna bouwt GitHub de app opnieuw. Zet de nieuwe versie op je telefoon.

## 5. Kijken of het werkt ✅
- Klok in en uit (of pas een dag aan bij Werk).
- Kijk in de spreadsheet: staat de regel van vandaag erin?
- Uitzetten kan in de app: **Instellingen ➜ 📊 Google Sheet**.

## Goed om te weten
- Lukt het een keer niet (geen internet)? De app probeert het later opnieuw, tot 14 dagen terug.
- Pas je een dag later aan in de app, terwijl hij al in de spreadsheet staat? Dan blijft de spreadsheet hetzelfde. Pas het daar zelf aan.
- Script veranderd? **Implementeren ➜ Implementaties beheren ➜ ✏️ ➜ Nieuwe versie**. De URL blijft dan hetzelfde.
