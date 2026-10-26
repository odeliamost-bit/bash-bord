# Sync tussen apparaten + echte potjes

Voor Claude Code. Zet dit stuk in `CLAUDE.md` (onder "Gemaakte keuzes") en volg de regels uit `CLAUDE.md`: eerst plan, dan "mag ik?", dan bouwen.

## 1. Sync tussen telefoon en tablet (gekozen door Odelia, okt 2026)

**Wat Odelia wil:** iets één keer invullen, en het staat op al haar apparaten. Nooit twee keer invullen.

**Apparaten:** Nothing Phone (1) en Android-tablet. Later misschien ook de laptop.

**Hoe:**
- Backend: **Cloudflare Workers + D1** (stond al als plan in `CLAUDE.md`). Mag kritisch bekeken worden; leg andere opties eenvoudig uit.
- Room blijft de opslag op het apparaat. De app werkt dus ook zonder internet.
- Sync zodra er internet is: bij openen van de app en elke 15 minuten op de achtergrond (WorkManager).
- Elk record heeft al een UUID en `updated_at`. Bij een conflict wint de nieuwste wijziging.
- Verwijderen: niet echt wissen, maar `verwijderd = true` zetten, zodat het andere apparaat het ook weet.
- Ook de JSON-bestanden (Geld, Leren, Wensen, enz.) moeten mee. Zet ze om naar Room of sync ze als geheel.
- Inloggen: één account voor Odelia, eenvoudig (bijv. inloggen met Google). Nooit wachtwoorden in de app opslaan.

**Let op, dit verandert een eerdere keuze:**
In `CLAUDE.md` staat: "Uren en afspraken gaan nooit online." Met sync gaan ze wél naar de eigen backend van Odelia.
- Vraag Odelia eerst: mag alles mee, of alleen sommige schermen?
- Pas daarna `CLAUDE.md` aan, en de CI-regel "netwerkcode alleen in `online/Online.kt`".
- Alles versleuteld (https), data alleen voor haar account.

**Stappen (klein, elk los te testen):**
1. Backend + inloggen.
2. Eén scherm syncen (bijv. Onthoud). Testen op telefoon én tablet.
3. Daarna de andere schermen één voor één.
4. Knop in instellingen: "Nu syncen" + wanneer de laatste sync was.

## 2. Potjes: echte bedragen (gekozen door Odelia)

De bedragen in `logica/GeldData.kt` (`GeldStart.potjes`) kloppen niet. Vervang ze door:

**Stap 1: eerst vaste bedragen**

| Potje | Bedrag |
|---|---|
| Vaste lasten 📱 | € 41,30 |
| Zorg 🩺 | € 180,00 |
| Snoep/lekkers 🍬 | € 50,00 |

**Stap 2: de rest in procenten**

| Potje | Procent | Echte rekening |
|---|---|---|
| Vrije geld 👛 | 20% | – |
| Investeren 📈 | 30% | Giro-rekening |
| Goede doelen/kerk ⛪ | 10% | – |
| Noodpot 🛟 | 20% | – |
| Spaarpot 🐷 | 20% | ING-spaarrekening |

Samen 100%.

**Regels:**
- Saldo blijft staan. Geen reset per maand.
- Spaarpot en Investeren zijn echte rekeningen; de rest zijn virtuele potjes.
- Pas ook `GeldTest.kt` aan op de nieuwe bedragen.
