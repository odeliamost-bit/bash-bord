# 📧 Urenmail: nu en later

## ✅ Nu (werkt zonder instellingen)

De app zet de urenmail klaar. **De app verstuurt nooit zelf.**

1. Vrijdag, 30 minuten na het uitklokken, komt er een melding: "Urenmail voor Kim staat klaar".
2. Tik erop. Kijk de uren na. Schrijf eventueel een opmerking.
3. Kies:
   - ✉️ **Open in Gmail**: Gmail opent met onderwerp "Urenregistratie" en de tekst erin. Jij drukt zelf op verzenden.
   - 📋 **Kopiëren**: open in Gmail de mail "Urenregistratie", tik **Beantwoorden** en plak. Zo blijft het in dezelfde draad.
   - 📤 **Delen**: naar een andere app.

Tip: vul één keer het e-mailadres van Kim in. Het blijft alleen op je telefoon.

⚠️ Wat nu nog niet kan:
- Het concept vanzelf **in de bestaande draad** zetten (daarom de Kopiëren-knop).
- Een echte **tabel** in de mail. Het worden nette regels: `ma 19-10-2026: 7:45 – 15:45 = 7:15 uur`.

## 🔧 Later: echte Gmail-koppeling

Dan zet de app zelf een **concept** in de draad "Urenregistratie", met een echte tabel. Versturen doe je nog steeds zelf.

Daar is een Google-instelling voor nodig. Die kan Claude niet voor je doen, want het gaat om jouw Google-account.

### Wat je dan instelt (eenmalig, ongeveer 30 minuten, samen met je broer)
1. Ga naar **console.cloud.google.com** en log in met je eigen Gmail-account.
2. Maak een **project**, bijvoorbeeld "Mijn dashboard".
3. Zet de **Gmail API** aan.
4. Stel het **toestemmingsscherm** in (OAuth consent screen):
   - type: **Extern**, status: **Testen**
   - testgebruiker: je eigen Gmail-adres
5. Maak een **OAuth-client** van het type **Android**:
   - pakketnaam: `nl.odelia.dashboard`
   - SHA-1: de vingerafdruk van de sleutel waarmee de app gebouwd wordt (je broer kan die vinden in Android Studio; Claude legt het dan uit)
6. Geef Claude daarna door dat het klaar is. Dan bouwt Claude in de app:
   - inloggen met Google (één keer toestemming geven)
   - de draad "Urenregistratie" zoeken
   - een concept-antwoord maken in die draad (alleen recht op **concepten maken**, niet op versturen)

### Goed om te weten
- 🔑 Er komt **geen geheime sleutel** in de code of in git. Een Android-client werkt met de pakketnaam en de SHA-1.
- 🔁 In de status "Testen" vraagt Google ongeveer **elke week opnieuw** om in te loggen. Dat is normaal voor apps die niet in de Play Store staan. Echt "goedgekeurd" worden kost veel werk (Google-controle). Voor jezelf is "Testen" genoeg.
- 🔒 Rechten die nodig zijn: Gmail-concepten maken en mails lezen om de draad te vinden (`gmail.compose` + `gmail.readonly`). De app gebruikt ze alleen voor de urenmail.

### Andere manier (ook mogelijk)
Een **Google Apps Script** in je eigen Google-account. Dat script zet het concept in de draad. De app stuurt dan alleen de uren naar het script. Dit is minder werk in de app, maar het script heeft een geheime link nodig. Die mag niet in git, en moet dus apart op de telefoon ingesteld worden.

👉 Claude's advies: eerst een paar weken de simpele manier gebruiken. Is Kopiëren + plakken te veel gedoe? Dan de echte koppeling.
