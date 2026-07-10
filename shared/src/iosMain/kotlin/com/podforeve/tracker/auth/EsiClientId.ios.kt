package com.podforeve.tracker.auth

import platform.Foundation.NSBundle

// Set via xcconfig: ESI_CLIENT_ID = your_client_id
// See: iosApp/Configuration/Secrets.xcconfig (gitignored)
internal actual fun esiClientId(): String = NSBundle.mainBundle.objectForInfoDictionaryKey("ESIClientID") as? String ?: ""
