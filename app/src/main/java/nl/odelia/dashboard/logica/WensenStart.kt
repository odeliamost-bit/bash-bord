package nl.odelia.dashboard.logica

import java.util.UUID

/**
 * Odelia's verlanglijst uit ontwerp/Wensen.dc.html, voor de eerste keer dat de app opent.
 * Ze koos zelf dat deze links in de (openbare) code staan. Niets persoonlijks erin.
 */
object WensenStart {
    private val rijen = listOf(
        Triple(WensSoort.Kleding, "🧦", "Schapenwollen sokken") to "",
        Triple(WensSoort.Kleding, "👟", "Barefoot schoenen") to "https://vaistoa.com/collections/autumn-barefoot-shoes/products/kajo-1-barefoot-chelsea-boots?variant=55379672891717",
        Triple(WensSoort.Kleding, "👕", "Supima-katoenen longsleeve") to "https://hikecarewinkel.nl/products/alpaca-ergonomische?variant=50742198436160",
        Triple(WensSoort.Kleding, "🔥", "Elektrische sokken") to "https://smartsporter.nl/products/ignite-verwarmde-sokken-wintersokken-met-verwarming",
        Triple(WensSoort.Kleding, "👡", "Open schoenen") to "",
        Triple(WensSoort.Haken, "⚪", "Poly pellets voor haken") to "",
        Triple(WensSoort.Haken, "💍", "Ring om te haken") to "https://www.amazon.nl/gp/product/B0C8RR66NM",
        Triple(WensSoort.Haken, "🧶", "Yarn winder") to "",
        Triple(WensSoort.Haken, "🐭", "Patroon: muis pop") to "https://www.etsy.com/nl/listing/1791061165/brie-muis-zeemeerminstaart-mod",
        Triple(WensSoort.Haken, "🦌", "Patroon: hert pop") to "https://www.etsy.com/nl/listing/1850608502/fora-fawn-fairy-mod-haakpatroon",
        Triple(WensSoort.Haken, "🐉", "Patroon: roze draak") to "https://www.etsy.com/nl/listing/1759631991/fairy-dragon-amigurumi-haakpatroon",
        Triple(WensSoort.Haken, "🔖", "Patroon: boekenlegger") to "https://www.etsy.com/nl/listing/1890221875/6-in-1-gehaakte-bladwijzer-gehaakte",
        Triple(WensSoort.Haken, "🌻", "Patroon: zonnebloem rugzak") to "https://www.etsy.com/nl/listing/1714068971/lees-eerst-disclaimer-zonnebloem",
        Triple(WensSoort.Huis, "🟣", "Ubbi siliconen bewaarzak") to "https://www.amazon.nl/Ubbi-Platemate-Reusable-Pomegranate-Buttercup/dp/B0GR21FNKM",
        Triple(WensSoort.Huis, "🪟", "Gordijnbeugel") to "",
        Triple(WensSoort.Huis, "🛏️", "Beddengoed") to "",
        Triple(WensSoort.Huis, "🍳", "Stalen pan") to "https://www.dartointernational.com/product/Sautepan-n27/1?cs=true&cst=custom",
        Triple(WensSoort.Huis, "☕", "Glazen mok") to "https://www.nordicnest.nl/merken/muurla/muurla-glazen-mok-35-cl-/?variantId=514249-01",
        Triple(WensSoort.Huis, "🪞", "360 verstelbare spiegel") to "https://amzn.eu/d/0gGIPHH3",
        Triple(WensSoort.Verzorging, "🧴", "Beef tallow huidverzorging") to "https://amzn.eu/d/btwCgKS",
        Triple(WensSoort.Verzorging, "💎", "Oorbelletjes laten schieten") to "",
        Triple(WensSoort.Verzorging, "💤", "Nekkussen") to "https://travel-detail.com/products/neck-pro-deluxe-comfort-reiskussen-met-ergonomische-nekondersteuning",
        Triple(WensSoort.Overig, "⌚", "Horlogebandjes") to "https://amzn.eu/d/0dhD8KHW",
        Triple(WensSoort.Overig, "🔋", "Nieuwe batterij laptop") to "",
        Triple(WensSoort.Overig, "🍅", "Cherry tomaat zaadjes") to "",
        Triple(WensSoort.Overig, "🎧", "Koptelefoonkussentjes") to "https://amzn.eu/d/hM4OHuH",
        Triple(WensSoort.Overig, "📱", "Digitaal boek") to "https://garbie.com/",
        Triple(WensSoort.Overig, "🎲", "Andromeda Escape spel") to "https://www.badrulegames.com/nl/games/andromeda-escape/",
        Triple(WensSoort.Overig, "🐴", "Equigroomer") to "https://equigroomer.nl/Equigroomer_small_natural",
    )

    /** Vaste id per wens, zodat de lijst nooit dubbel wordt (ook niet na synchroniseren). */
    val lijst: List<Wens> = rijen.mapIndexed { i, (r, link) ->
        val links = if (link.isEmpty()) emptyList() else listOf(Wensen.schoon(link))
        Wens(
            id = UUID.nameUUIDFromBytes("wens-start-$i".toByteArray()).toString(),
            naam = r.third,
            emoji = r.second,
            soort = r.first,
            winkel = links.firstOrNull()?.let { Wensen.winkel(it) },
            links = links,
        )
    }
}
