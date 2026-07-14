package com.podforeve.tracker.domain.usecase

// Fixed EVE game-data volumes (m³ per unit) for PI items. P-tier volumes are
// invariant — they won't change without a CCP patch that would require a client update anyway.
internal object PiVolumeTable {
    // Returns 0.0 for unknown type IDs — unknown items are excluded rather than guessed wrong.
    fun volumeOf(typeId: Int): Double = volumes[typeId] ?: 0.0

    fun capacityOf(pinTypeId: Int): Double = when (pinTypeId) {
        in launchpadTypeIds -> 10_000.0
        in storageFacilityTypeIds -> 12_000.0
        else -> 0.0
    }

    val storageTypeIds get() = launchpadTypeIds + storageFacilityTypeIds

    // Launchpad type IDs (10,000 m³) — planet-type-specific; verified from ESI colony data 2026-07-13.
    val launchpadTypeIds = setOf(
        2544,  // Barren
        2555,  // Lava
        2557,  // Storm
        2543,  // Gas
        2256,  // Temperate
        // Oceanic, Plasma, Ice — TBD when ESI data available
    )

    // Storage Facility type IDs (12,000 m³) — planet-type-specific; verified from ESI colony data 2026-07-13.
    val storageFacilityTypeIds = setOf(
        2541,  // Barren
        2558,  // Lava
        2561,  // Storm
        2536,  // Gas
        2562,  // Temperate
        // Oceanic, Plasma, Ice — TBD when ESI data available
    )

    private val volumes: Map<Int, Double> = buildMap {
        // P0 Raw Resources — 0.005 m³/u (verified via EVE Ref SDE)
        // 2308 = Suspended Plasma (Storm planet P0, was missing → fallback caused 20x inflation)
        listOf(2267, 2073, 2268, 2288, 2287, 2272, 2305, 2306, 2286, 3779, 2308, 2309, 2310, 2311, 2312, 9828)
            .forEach { put(it, 0.005) }
        // P1 Basic Commodities — 0.19 m³/u (verified: 3000 P0 → 20 P1, batch volume 3.8 m³)
        // 3645, 3683 = P1 outputs observed in ESI colony routes
        // 2394 = in P1 type_id range, included as precaution
        listOf(2389, 2390, 2391, 2392, 2393, 2394, 2395, 2396, 2397, 2398, 2399, 2400, 2401, 2402, 2403, 2404,
            3645, 3683)
            .forEach { put(it, 0.19) }
        // P2 Refined Commodities — 1.5 m³/u
        // 2463, 3828, 9838 = P2 outputs (qty 5/cycle) observed in ESI; not in 2327-2350 base range
        listOf(2327, 2328, 2329, 2330, 2331, 2332, 2333, 2334, 2335, 2336, 2337, 2338,
            2339, 2340, 2341, 2342, 2343, 2344, 2345, 2346, 2347, 2348, 2349, 2350,
            2463, 3828, 9838)
            .forEach { put(it, 1.5) }
        // P3 Specialized Commodities — 6.0 m³/u
        listOf(2351, 2352, 2353, 2354, 2358, 2360, 2361, 2362, 2363, 2364, 2366, 2367,
            2368, 2369, 2370, 2371, 2372, 2373, 2374, 2375, 2376)
            .forEach { put(it, 6.0) }
        // P4 Advanced Commodities — 100.0 m³/u
        listOf(2377, 2378, 2379, 2380, 2381, 2382, 2383, 2384)
            .forEach { put(it, 100.0) }
    }
}
