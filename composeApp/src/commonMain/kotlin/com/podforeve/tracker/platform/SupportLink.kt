package com.podforeve.tracker.platform

const val SUPPORT_DEVELOPMENT_URL = "https://buymeacoffee.com/viktor.shavarin"

// iOS stays false until App Store's treatment of external donation links for this app's category
// is confirmed (Guideline 3.2.1(vii) excludes "Games"); see [[Guide - App Store Launch Readiness]].
expect val supportsExternalSupportLink: Boolean
