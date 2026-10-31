/**
 * Uren uit de app (bash-bord) in deze spreadsheet zetten.
 * Hoort bij "Cargill_Registratie Werktijden_Odelia". Uitleg: hulpmiddelen/sheet/README.md.
 *
 * - Zoekt per dag de regel met die datum in kolom A.
 * - Vult alleen Begin (B), Einde (C) en pauze (D). Vrij of ziek: dat woord in Begin.
 * - Staat er al iets in Begin of Einde? Dan blijft het staan (gekozen door Odelia).
 * - Totaal, opmerkingen en de loon-kolommen worden nooit aangeraakt.
 * - Alleen met de geheime code (Scripteigenschap TOKEN). Er staat geen code in dit bestand.
 */
function doPost(e) {
  var eigenschappen = PropertiesService.getScriptProperties();
  var token = eigenschappen.getProperty('TOKEN');
  var bericht;
  try {
    bericht = JSON.parse(e.postData.contents);
  } catch (fout) {
    return antwoord({ ok: false, fout: 'kapot bericht' });
  }
  if (!token || bericht.token !== token) return antwoord({ ok: false, fout: 'verkeerde code' });

  var slot = LockService.getScriptLock();
  slot.waitLock(20000);
  try {
    var boek = SpreadsheetApp.getActiveSpreadsheet();
    var blad = boek.getSheetByName(eigenschappen.getProperty('BLAD') || 'Sheet1');
    if (!blad) return antwoord({ ok: false, fout: 'blad niet gevonden' });
    var zone = boek.getSpreadsheetTimeZone();
    var waarden = blad.getRange(1, 1, blad.getLastRow(), 3).getValues();

    // Datum (jjjj-mm-dd) -> regelnummer (vanaf 0).
    var regelVan = {};
    for (var i = 0; i < waarden.length; i++) {
      var a = waarden[i][0];
      if (a instanceof Date) regelVan[Utilities.formatDate(a, zone, 'yyyy-MM-dd')] = i;
    }

    var uit = { ok: true, ingevuld: [], bleef: [], niet: [] };
    (bericht.dagen || []).forEach(function (d) {
      var r = regelVan[d.datum];
      if (r === undefined) { uit.niet.push(d.datum); return; }
      if (waarden[r][1] !== '' || waarden[r][2] !== '') { uit.bleef.push(d.datum); return; }
      if (d.soort === 'gewerkt') {
        blad.getRange(r + 1, 2, 1, 3).setValues([[d.begin, d.eind, d.pauze]]);
      } else if (d.soort === 'vrij' || d.soort === 'ziek') {
        blad.getRange(r + 1, 2).setValue(d.soort);
      } else {
        uit.niet.push(d.datum);
        return;
      }
      uit.ingevuld.push(d.datum);
    });
    return antwoord(uit);
  } finally {
    slot.releaseLock();
  }
}

function antwoord(o) {
  return ContentService.createTextOutput(JSON.stringify(o)).setMimeType(ContentService.MimeType.JSON);
}
