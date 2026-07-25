@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.demo

import com.podforeve.tracker.domain.model.ColonySummary
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.WalletJournalEntry
import com.podforeve.tracker.ui.viewmodel.DashboardData
import kotlin.time.Clock

// Static sample data for Demo Mode — every screen renders from this object alone, with
// no ESI or local-DB access. Timestamps are relative to "now" so relative-time text
// (e.g. "2h ago", "3d 19h") never looks stale no matter when Demo Mode is entered.
// See wiki: [[ADR-022 - Demo Mode]], AuthState.Demo, AuthRepository.enterDemoMode().
object DemoData {
    private val now = Clock.System.now().epochSeconds

    private val activeSkill = SkillQueueEntry(
        queuePosition = 1, characterId = 0L, skillId = 3456,
        skillName = "Coherent Ore Processing", finishedLevel = 5,
        startSp = 24_000, finishSp = 1_280_000,
        startDate = now - 86_400, finishDate = now + 9 * 86_400,
    )

    val skillQueue: List<SkillQueueEntry> = listOf(
        activeSkill,
        SkillQueueEntry(2, 0L, 3453, "Simple Ore Processing", 3, 3_000, 90_000, null, null),
        SkillQueueEntry(3, 0L, 3453, "Simple Ore Processing", 4, 4_000, 270_000, null, null),
        SkillQueueEntry(4, 0L, 3453, "Simple Ore Processing", 5, 5_000, 810_000, null, null),
        SkillQueueEntry(5, 0L, 12195, "Graviton Physics", 3, 6_000, 90_000, null, null),
        SkillQueueEntry(6, 0L, 12195, "Graviton Physics", 4, 7_000, 270_000, null, null),
        SkillQueueEntry(7, 0L, 3446, "Retail", 3, 8_000, 90_000, null, null),
        SkillQueueEntry(8, 0L, 3446, "Retail", 4, 9_000, 270_000, null, null),
        SkillQueueEntry(9, 0L, 16622, "Marketing", 4, 10_000, 270_000, null, null),
        SkillQueueEntry(10, 0L, 16597, "Accounting", 4, 11_000, 270_000, null, null),
    )

    private val walletJournal = listOf(
        WalletJournalEntry(1, "bounty_prizes", 4_250_000.0, now - 3_600),
        WalletJournalEntry(2, "market_transaction", -12_500_000.0, now - 7_200),
        WalletJournalEntry(3, "pi_export_tax", -890_000.0, now - 86_400),
        WalletJournalEntry(4, "contract_reward", 18_000_000.0, now - 172_800),
    )

    val dashboard = DashboardData(
        name = "Nova Kestrel",
        portraitUrl = "",
        iskBalance = 128_400_000.0,
        securityStatus = 4.8,
        corporationName = "Nakugard Industries",
        totalSp = 46_300_000L,
        activeSkill = activeSkill,
        walletJournal = walletJournal,
    )

    // PiScreen only renders Planet.status() when colony == null (never true here — every
    // demo planet has colony data, same as a real active PI setup); the header instead
    // shows ExtractorCountdown, colored purely off extractorExpiryEpochSeconds vs now. These
    // three cover its three color states: stopped (red), <8h left (amber), healthy (green).
    val planets: List<Planet> = listOf(
        Planet(
            planetId = 40132050,
            characterId = 0L,
            planetName = "Nakugard I",
            planetType = "barren",
            lastUpdateEpochSeconds = now - 3_600,
            upgradeLevel = 4,
            colony = ColonySummary(
                extractorExpiryEpochSeconds = now + 2 * 86_400 + 19 * 3_600,
                runningFactories = 3,
                totalFactories = 3,
                sfCapacityM3 = 12_000.0,
                sfUsedM3 = 1_572.0,
                lpCapacityM3 = 10_000.0,
                lpUsedM3 = 60.0,
                dataFetchedAtEpochSeconds = now - 180,
            ),
        ),
        Planet(
            planetId = 40132051,
            characterId = 0L,
            planetName = "Nakugard II",
            planetType = "lava",
            lastUpdateEpochSeconds = now - 1_800,
            upgradeLevel = 4,
            colony = ColonySummary(
                extractorExpiryEpochSeconds = now - 1_600,
                runningFactories = 0,
                totalFactories = 3,
                sfCapacityM3 = 12_000.0,
                sfUsedM3 = 856.0,
                lpCapacityM3 = 10_000.0,
                lpUsedM3 = 52.5,
                dataFetchedAtEpochSeconds = now - 420,
            ),
        ),
        Planet(
            planetId = 40132079,
            characterId = 0L,
            planetName = "Nakugard VI",
            planetType = "temperate",
            lastUpdateEpochSeconds = now - 900,
            upgradeLevel = 4,
            colony = ColonySummary(
                extractorExpiryEpochSeconds = now + 5 * 3_600,
                runningFactories = 2,
                totalFactories = 2,
                sfCapacityM3 = 24_000.0,
                sfUsedM3 = 6_041.0,
                lpCapacityM3 = 20_000.0,
                lpUsedM3 = 1_102.0,
                dataFetchedAtEpochSeconds = now - 600,
            ),
        ),
    )

    val industryJobs: List<IndustryJob> = listOf(
        IndustryJob(1001, 0L, 1, "Stabber Blueprint", 5, now - 7_200, now + 18_000, "active"),
        IndustryJob(1002, 0L, 4, "Mining Barge Blueprint", 1, now - 86_400, now + 86_400, "active"),
        IndustryJob(1003, 0L, 8, "Catalyst Blueprint", 10, now - 90_000, now - 600, "delivered"),
        IndustryJob(1004, 0L, 1, "Procurer Blueprint", 3, now - 259_200, now - 43_200, "delivered"),
    )
}
