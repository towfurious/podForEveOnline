package com.podforeve.tracker.domain.model

data class WalletJournalEntry(
    val id: Long,
    val refType: String,
    val amount: Double,
    val dateEpochSeconds: Long,
) {
    val displayName: String get() = when (refType) {
        "bounty_prizes"                   -> "Bounty prizes"
        "market_transaction"              -> "Market transaction"
        "pi_export_tax"                   -> "PI export tax"
        "pi_purchase"                     -> "PI purchase"
        "contract_price"                  -> "Contract price"
        "player_donation"                 -> "Transfer"
        "manufacturing"                   -> "Manufacturing fee"
        "research_fee"                    -> "Research fee"
        "agent_mission_reward"            -> "Mission reward"
        "agent_mission_time_bonus_reward" -> "Mission bonus"
        "skill_purchase"                  -> "Skill purchase"
        "structure_gate_jump"             -> "Jump fee"
        else -> refType.split("_")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}
