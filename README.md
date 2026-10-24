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

## 🗂️ Wat zit waar

| Map | Wat |
|---|---|
| `app/src/main/java/.../ui/thuis/` | Hoofdscherm: welkomkaart, tabjes, tegels |
| `app/src/main/java/.../ui/menu/` | Zijbalk (menu) |
| `app/src/main/java/.../ui/onderdelen/` | Bouwsteentjes: klei-kaarten, icoontjes |
| `app/src/main/java/.../ui/theme/` | Kleuren en lettertype (Nunito) |
| `app/src/main/res/` | Plaatje, lettertype, app-icoon |
| `ontwerp/` | Het goedgekeurde ontwerp |
| `hulpmiddelen/voorproef/` | Hulpje voor Claude om te checken zonder telefoon |
| `licenties/` | Licentie van het lettertype Nunito (vrij te gebruiken) |
