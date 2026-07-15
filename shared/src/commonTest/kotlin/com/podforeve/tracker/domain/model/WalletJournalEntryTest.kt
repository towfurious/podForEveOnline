package com.podforeve.tracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class WalletJournalEntryTest {
    private fun entry(refType: String, amount: Double = 0.0) = WalletJournalEntry(
        id = 1L,
        refType = refType,
        amount = amount,
        dateEpochSeconds = 0,
    )

    @Test
    fun marketTransactionLabelDependsOnAmountSign() {
        assertEquals("Market sale", entry("market_transaction", amount = 901_200_000.0).displayName)
        assertEquals("Market sale", entry("market_transaction", amount = 0.0).displayName) // boundary: zero counts as sale
        assertEquals("Market buy", entry("market_transaction", amount = -1_300_000.0).displayName)
    }

    @Test
    fun knownRefTypesMapToReadableLabels() {
        assertEquals("Bounty prizes", entry("bounty_prizes").displayName)
        assertEquals("PI export tax", entry("pi_export_tax").displayName)
        assertEquals("PI purchase", entry("pi_purchase").displayName)
        assertEquals("PI construction", entry("planetary_construction").displayName)
        assertEquals("Sales tax", entry("transaction_tax").displayName)
        assertEquals("AIR reward", entry("air_career_program_reward").displayName)
        assertEquals("Contract", entry("contract_price").displayName)
        assertEquals("Transfer", entry("player_donation").displayName)
        assertEquals("Manufacturing fee", entry("manufacturing").displayName)
        assertEquals("Research fee", entry("research_fee").displayName)
        assertEquals("Mission reward", entry("agent_mission_reward").displayName)
        assertEquals("Mission bonus", entry("agent_mission_time_bonus_reward").displayName)
        assertEquals("Skill purchase", entry("skill_purchase").displayName)
        assertEquals("Jump fee", entry("structure_gate_jump").displayName)
    }

    @Test
    fun unknownRefTypeFallsBackToTitleCasedWords() {
        assertEquals("Some Unknown Type", entry("some_unknown_type").displayName)
        assertEquals("Foobar", entry("foobar").displayName)
    }
}
