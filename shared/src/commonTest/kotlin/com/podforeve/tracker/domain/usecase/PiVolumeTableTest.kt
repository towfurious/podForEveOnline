package com.podforeve.tracker.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PiVolumeTableTest {

    @Test
    fun volumeOfP0RawResourcesIsHalfAHundredthCubicMeter() {
        // 2308 = Suspended Plasma — the P0 type that was missing from this table and caused a
        // 20x volume-inflation bug on Storm planets (see log.md 2026-07-11). Locking it down
        // specifically, not just "a P0 id", so a future edit can't silently drop it again.
        assertEquals(0.005, PiVolumeTable.volumeOf(2308))
        assertEquals(0.005, PiVolumeTable.volumeOf(2267))
    }

    @Test
    fun volumeOfP1BasicCommoditiesIsPoint19CubicMeters() {
        assertEquals(0.19, PiVolumeTable.volumeOf(3645))
        assertEquals(0.19, PiVolumeTable.volumeOf(2389))
    }

    @Test
    fun volumeOfP2RefinedCommoditiesIs1Point5CubicMeters() {
        assertEquals(1.5, PiVolumeTable.volumeOf(2463))
        assertEquals(1.5, PiVolumeTable.volumeOf(2327))
    }

    @Test
    fun volumeOfP3SpecializedCommoditiesIs6CubicMeters() {
        assertEquals(6.0, PiVolumeTable.volumeOf(2351))
    }

    @Test
    fun volumeOfP4AdvancedCommoditiesIs100CubicMeters() {
        assertEquals(100.0, PiVolumeTable.volumeOf(2377))
    }

    @Test
    fun volumeOfUnknownTypeIdIsZeroNotGuessed() {
        // Unknown items are excluded rather than falling back to a guessed volume — a wrong
        // guess previously caused a 2x storage-fill bug (see log.md 2026-07-11).
        assertEquals(0.0, PiVolumeTable.volumeOf(999_999))
    }

    @Test
    fun capacityOfLaunchpadPinsIs10000CubicMeters() {
        assertEquals(10_000.0, PiVolumeTable.capacityOf(2544)) // Barren launchpad
    }

    @Test
    fun capacityOfStorageFacilityPinsIs12000CubicMeters() {
        assertEquals(12_000.0, PiVolumeTable.capacityOf(2541)) // Barren storage facility
    }

    @Test
    fun capacityOfNonStorageTypeIdIsZero() {
        assertEquals(0.0, PiVolumeTable.capacityOf(2524)) // Barren command center — not storage
    }

    @Test
    fun storageTypeIdsIsTheUnionOfLaunchpadAndStorageFacilityIds() {
        assertTrue(PiVolumeTable.storageTypeIds.containsAll(PiVolumeTable.launchpadTypeIds))
        assertTrue(PiVolumeTable.storageTypeIds.containsAll(PiVolumeTable.storageFacilityTypeIds))
        assertEquals(
            PiVolumeTable.launchpadTypeIds.size + PiVolumeTable.storageFacilityTypeIds.size,
            PiVolumeTable.storageTypeIds.size,
        ) // no id should appear in both sets
    }
}
