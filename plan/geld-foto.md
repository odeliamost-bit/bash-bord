# Plan (later): foto lezen in Geld

Status: **plan, nog niet gebouwd.** Eerst de app een paar dagen testen.

## Wat Odelia wil
Een foto sturen (bonnetje of bankscherm). De app leest bedrag en winkel en stelt het goede potje voor. Zij tikt op **Ja**. Pas dan komt het in Geld.

## Wat er nodig is

### Van Odelia en haar broer
1. API-sleutel van Anthropic (console.anthropic.com). Los van het Claude-abonnement, betalen per foto.
2. Gratis Cloudflare-account met een klein programma (Worker) dat de sleutel bewaart. De sleutel komt **nooit** in de app of in git.
3. Een duidelijk "ja" voor online. Nu gaat Geld nooit online.
4. Haar regels voor de potjes (bv. snoep naar "Snoep/lekkers").

### Van Claude (bouwen)
- Fotoknop in Geld (camera en kiezen; zelfde aanpak als de foto bij Onthoud).
- Koppeling met de Worker, alleen na toestemming (zoals `OnlineVraag` bij Zeilweer). Alleen naar de eigen Worker, https, met maximum grootte.
- Scherm met het voorstel: bedrag, winkel, potje. Knoppen: Ja, Aanpassen, Weg.
- Foto verkleinen, niet bewaren na lezen (tenzij zij het wil).
- Testjes voor het uitlezen van het antwoord en de potje-regels.

## Regels
- Nooit iets in Geld zetten zonder haar "ja".
- Geen echte bedragen of rekeningnummers in code, testdata of git.
- CI-controle uitbreiden: netwerkcode alleen in `online/`, alleen naar de eigen Worker.
- Eerlijk zeggen dat lezen kan misgaan; zij controleert altijd.

## Volgorde
1. Besluiten: Worker en sleutel (broer).
2. Worker + test met verzonnen foto.
3. Fotoknop en voorstel-scherm.
4. Testen op tablet en telefoon.

Hoort bij: Leren met Claude (zelfde backend kan dat later ook).
