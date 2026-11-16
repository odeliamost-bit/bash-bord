package nl.odelia.dashboard.logica

import java.util.UUID

/**
 * Aanvulling op de wensen (nov 2026, gevraagd door Odelia):
 * - alle links uit haar Google Doc "Verlanglijst" bij de goede wens (eerst stond alleen de eerste link erin);
 * - wensen uit eerdere gesprekken met Claude die nog niet in de app stonden;
 * - per wens een prijs-schatting (± bedrag), bovenaan de kaart.
 *
 * Gaat één keer over haar lijst heen ([WensenData.aanvulling] onthoudt dat).
 * Zelfde wens twee keer in de lijst (bijv. na de Kompas-import)? Dan wordt het één wens met alle links.
 * Prijzen zijn schattingen, geen echte prijzen van vandaag.
 */
object WensenAanvulling {
    const val VERSIE = 1

    private class Rij(
        /** Stukjes tekst waaraan de wens in haar lijst te herkennen is (kleine letters). */
        val zoek: List<String>,
        val naam: String,
        val emoji: String,
        val soort: WensSoort,
        val prijs: String,
        val links: List<String> = emptyList(),
        /** Oude links die weg mogen (bijv. afgekapt). */
        val weg: List<String> = emptyList(),
    )

    private val rijen = listOf(
        // 👕 Kleding
        Rij(listOf("schapenwol", "schapen wol"), "Schapenwollen sokken", "🧦", WensSoort.Kleding, "± € 15"),
        Rij(
            listOf("barefoot", "bare voet", "blootvoet"), "Barefoot schoenen", "👟", WensSoort.Kleding, "± € 100–130",
            listOf(
                "https://vaistoa.com/collections/autumn-barefoot-shoes/products/kajo-1-barefoot-chelsea-boots?variant=55379672891717",
                "https://vaistoa.com/collections/autumn-barefoot-shoes/products/kajo-barefoot-chelsea-boots?variant=58767386870085",
            ),
        ),
        Rij(
            listOf("supima", "longsleeve"), "Supima-katoenen longsleeve", "👕", WensSoort.Kleding, "± € 40",
            listOf("https://hikecarewinkel.nl/products/alpaca-ergonomische?variant=50742198436160"),
        ),
        Rij(
            listOf("elektrische sok", "verwarmde sok"), "Elektrische sokken", "🔥", WensSoort.Kleding, "± € 60–100",
            listOf(
                "https://smartsporter.nl/products/ignite-verwarmde-sokken-wintersokken-met-verwarming",
                "https://heatdryclothing.com/verwarmde-kleding/product/verwarmde-sokken/verwarmde-sokken-triheat-max-met-afstandsbediening/?attribute_pa_maat=43-46",
            ),
        ),
        Rij(listOf("open schoen"), "Open schoenen", "👡", WensSoort.Kleding, "± € 60"),
        Rij(listOf("laarsjes", "hoge laars"), "Warme, hoge laarsjes", "👢", WensSoort.Kleding, "± € 80"),
        Rij(
            listOf("ondergoed", "pyjama"), "Zacht ondergoed + pyjama (weinig naden, duurzaam)", "🩲", WensSoort.Kleding, "± € 60",
        ),

        // 🧶 Haken
        Rij(listOf("poly pellets", "pellets"), "Poly pellets voor haken", "⚪", WensSoort.Haken, "± € 8"),
        Rij(
            listOf("ring om te haken", "haakring"), "Ring om te haken", "💍", WensSoort.Haken, "± € 10",
            listOf("https://www.amazon.nl/gp/product/B0C8RR66NM"),
        ),
        Rij(listOf("yarn winder", "wolwinder"), "Yarn winder", "🧶", WensSoort.Haken, "± € 20"),
        Rij(
            listOf("muis pop", "patroon: muis"), "Patroon: muis pop", "🐭", WensSoort.Haken, "± € 6",
            listOf("https://www.etsy.com/nl/listing/1791061165/brie-muis-zeemeerminstaart-mod"),
        ),
        Rij(
            listOf("hert pop", "patroon: hert"), "Patroon: hert pop", "🦌", WensSoort.Haken, "± € 6",
            listOf("https://www.etsy.com/nl/listing/1850608502/fora-fawn-fairy-mod-haakpatroon"),
        ),
        Rij(
            listOf("roze draak", "patroon: draak"), "Patroon: roze draak", "🐉", WensSoort.Haken, "± € 6",
            listOf("https://www.etsy.com/nl/listing/1759631991/fairy-dragon-amigurumi-haakpatroon"),
        ),
        Rij(
            listOf("boekenlegger", "bladwijzer"), "Patroon: boekenlegger", "🔖", WensSoort.Haken, "± € 5",
            listOf("https://www.etsy.com/nl/listing/1890221875/6-in-1-gehaakte-bladwijzer-gehaakte"),
        ),
        Rij(
            listOf("zonnebloem"), "Patroon: zonnebloem rugzak", "🌻", WensSoort.Haken, "± € 6",
            listOf("https://www.etsy.com/nl/listing/1714068971/lees-eerst-disclaimer-zonnebloemrugzak"),
            weg = listOf("https://www.etsy.com/nl/listing/1714068971/lees-eerst-disclaimer-zonnebloem"),
        ),

        // 🏠 Huis
        Rij(
            listOf("ubbi", "bewaarzak"), "Ubbi siliconen bewaarzak", "🟣", WensSoort.Huis, "± € 15",
            listOf("https://www.amazon.nl/Ubbi-Platemate-Reusable-Pomegranate-Buttercup/dp/B0GR21FNKM"),
        ),
        Rij(listOf("gordijn"), "Gordijnbeugel", "🪟", WensSoort.Huis, "± € 10"),
        Rij(listOf("beddengoed", "dekbedovertrek"), "Beddengoed", "🛏️", WensSoort.Huis, "± € 50"),
        Rij(
            listOf("stalen pan", "stalenpan", "sautepan"), "Stalen pan", "🍳", WensSoort.Huis, "± € 90",
            listOf(
                "https://www.dartointernational.com/product/Sautepan-n27/1?cs=true&cst=custom",
                "https://youtube.com/shorts/nWSb7VQZy_0",
            ),
        ),
        Rij(
            listOf("glazen mok"), "Glazen mok", "☕", WensSoort.Huis, "± € 15",
            listOf("https://www.nordicnest.nl/merken/muurla/muurla-glazen-mok-35-cl-/?variantId=514249-01"),
        ),
        Rij(
            listOf("spiegel", "mirror"), "360 verstelbare spiegel", "🪞", WensSoort.Huis, "± € 30",
            listOf("https://amzn.eu/d/0gGIPHH3"),
        ),
        Rij(
            listOf("eenpersoonsbed", "hoofdbord"), "Eenpersoonsbed met gestoffeerd hoofdbord", "🛏️", WensSoort.Huis, "± € 300–500",
        ),
        Rij(listOf("perliet"), "Zakje perliet", "🪴", WensSoort.Huis, "± € 5"),

        // 🧴 Verzorging
        Rij(
            listOf("tallow"), "Beef tallow huidverzorging", "🧴", WensSoort.Verzorging, "± € 20",
            listOf("https://amzn.eu/d/btwCgKS"),
        ),
        Rij(listOf("oorbel"), "Oorbelletjes laten schieten", "💎", WensSoort.Verzorging, "± € 30"),
        Rij(
            listOf("nekkussen", "nekondersteuning"), "Nekkussen", "💤", WensSoort.Verzorging, "± € 40",
            listOf("https://travel-detail.com/products/neck-pro-deluxe-comfort-reiskussen-met-ergonomische-nekondersteuning"),
        ),

        // ✨ Overig
        Rij(
            listOf("horloge"), "Horlogebandjes", "⌚", WensSoort.Overig, "± € 10–15 per stuk",
            listOf(
                "https://amzn.eu/d/0dhD8KHW",
                "https://amzn.eu/d/06GsabWc",
                "https://amzn.eu/d/0eJD2zlh",
                "https://amzn.eu/d/05rsUYkC",
            ),
        ),
        Rij(listOf("batterij"), "Nieuwe batterij laptop", "🔋", WensSoort.Overig, "± € 60"),
        Rij(listOf("tomaat", "tomato"), "Cherry tomaat zaadjes (Gardener's Delight)", "🍅", WensSoort.Overig, "± € 3"),
        Rij(
            listOf("koptelefoon"), "Koptelefoonkussentjes", "🎧", WensSoort.Overig, "± € 15",
            listOf("https://amzn.eu/d/hM4OHuH"),
        ),
        Rij(listOf("digitaal boek", "garbie"), "Digitaal boek", "📱", WensSoort.Overig, "prijs nog checken", listOf("https://garbie.com/")),
        Rij(
            listOf("andromeda"), "Andromeda Escape spel", "🎲", WensSoort.Overig, "± € 35",
            listOf("https://www.badrulegames.com/nl/games/andromeda-escape/"),
        ),
        Rij(
            listOf("equigroomer"), "Equigroomer", "🐴", WensSoort.Overig, "± € 25",
            listOf("https://equigroomer.nl/Equigroomer_small_natural"),
        ),
        Rij(
            listOf("zeilboot", "randmeer"), "Zeilboot-modelletje (Randmeer, of Laser, Splash, Valk)", "⛵", WensSoort.Overig,
            "± € 27", listOf("https://www.artitecshop.com/"),
        ),
        Rij(
            listOf("filament", "panchroma"), "Filament Polymaker Panchroma Marble Sandstone (3DJake)", "🧵", WensSoort.Overig, "± € 21 per kg",
        ),
        Rij(
            listOf("rugzak voor werk", "werkrugzak", "werk-rugzak", "tradesman", "packout", "fatmax"),
            "Werkrugzak (Klein, Milwaukee, Veto, Stanley)", "🎒", WensSoort.Overig, "± € 75–375",
            listOf(
                "https://kleintools.eu/product/55475",
                "https://benl.rs-online.com/web/p/tool-bags/2698979/",
                "https://benl.rs-online.com/web/p/tool-bags/2698982/",
                "https://www.dexis.be/en/gereedschaps-rugzak-packout-4932471131-milwaukee/03042057",
                "https://www.toolfix.ie/p/milwaukee-packout-backpack-4932471131/4932471131",
                "https://toolbrothers.nl/en/products/veto-pro-pac-tech-pac-rucksack-werkzeugtasche-25-x-33-x-48-3-cm-vpp10070",
                "https://www.alternate.nl/STANLEY/FATMAX-Rugzak-Quick-Access/html/product/1940634",
                "https://nl.rubix.com/nl/stanley-fatmax-rugzak-quick-access/p-G3510027525",
            ),
        ),
    )

    /** Voegt links en prijzen toe, maakt dubbele wensen één, en zet nieuwe wensen achteraan. */
    fun pas(lijst: List<Wens>): List<Wens> {
        var uit = lijst
        rijen.forEachIndexed { i, r ->
            val schoon = r.links.map { Wensen.schoon(it) }
            val passend = uit.filter { w -> r.zoek.any { w.naam.lowercase().contains(it) } }
            if (passend.isEmpty()) {
                uit = uit + Wens(
                    id = UUID.nameUUIDFromBytes("wens-aanvulling-$i".toByteArray()).toString(),
                    naam = r.naam,
                    emoji = r.emoji,
                    soort = r.soort,
                    winkel = schoon.firstNotNullOfOrNull { Wensen.winkel(it) },
                    links = schoon,
                    prijs = r.prijs,
                )
            } else {
                // Eén wens houden: die met de meeste hartjes. Alle links bij elkaar.
                val houden = passend.maxBy { it.hartjes }
                val weg = r.weg.map { Wensen.schoon(it) }.toSet()
                val links = (passend.flatMap { it.links } + schoon).filterNot { it in weg }.distinct()
                val samen = houden.copy(
                    links = links,
                    winkel = houden.winkel ?: links.firstNotNullOfOrNull { Wensen.winkel(it) },
                    prijs = houden.prijs ?: r.prijs,
                    gekocht = passend.all { it.gekocht },
                )
                val andere = passend.map { it.id }.toSet() - houden.id
                uit = uit.filterNot { it.id in andere }.map { if (it.id == houden.id) samen else it }
            }
        }
        return uit
    }
}
