package com.podforeve.tracker.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// User-facing opt-out for the skill-training live-countdown notification (the persistent
// Android ForegroundService shade entry — see [[ADR-006]]/[[ADR-015]]). Defaults to enabled
// so existing installs see no behavior change until the user explicitly flips it. Completion
// alerts (skill/job/extractor "finished") are a separate mechanism and are not affected by
// this preference — see NotificationScheduler.android.kt's reconcileSkillService() gate.
class NotificationPreferences(private val storage: SecureStorage) {

    private val _skillLiveCountdownEnabledFlow = MutableStateFlow(load())
    val skillLiveCountdownEnabledFlow: StateFlow<Boolean> get() = _skillLiveCountdownEnabledFlow

    var skillLiveCountdownEnabled: Boolean
        get() = _skillLiveCountdownEnabledFlow.value
        set(value) {
            _skillLiveCountdownEnabledFlow.value = value
            storage.write(SecureStorageKeys.SKILL_LIVE_NOTIFICATION_ENABLED, value.toString())
        }

    private fun load(): Boolean = storage.read(SecureStorageKeys.SKILL_LIVE_NOTIFICATION_ENABLED)?.toBooleanStrictOrNull() ?: true
}

// iOS has no live-countdown/ForegroundService notion at all (one-shot UNNotificationRequest
// only, per ADR-015) — this preference has nothing to gate there, so the settings row is
// hidden on iOS rather than shown as a no-op toggle.
expect val supportsSkillLiveCountdownNotification: Boolean
