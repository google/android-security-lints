/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

/*
 * The list of issues that will be checked when running <code>lint</code>.
 */
class IssueRegistry : IssueRegistry() {
    override val issues = listOf(
        BadCryptographyUsageDetector.VULNERABLE_ALGORITHM_ISSUE,
        BadCryptographyUsageDetector.UNSAFE_ALGORITHM_USAGE_ISSUE,
        CustomSchemeDetector.AUTOVERIFY_ATTRIBUTE_ISSUE,
        DnsConfigDetector.SDK_LEVEL_ISSUE,
        StrandhoggDetector.ISSUE,
        TapjackingDetector.ISSUE,
        MissingNetworkSecurityConfigDetector.CLEARTEXT_TRAFFIC_ISSUE,
        MissingNetworkSecurityConfigDetector.TRUSTED_USER_CERTS_ISSUE,
        UnintendedExposedUrlDetector.EXPOSED_URL_ISSUE,
        UnintendedExposedUrlDetector.PRIVATE_IP_ADDRESS_ISSUE,
        MisconfiguredFileProviderDetector.ROOT_PATH_ISSUE,
        MisconfiguredFileProviderDetector.EXTERNAL_PATH_ISSUE,
        MisconfiguredFileProviderDetector.DOT_PATH_ISSUE,
        MisconfiguredFileProviderDetector.SLASH_PATH_ISSUE,
        MisconfiguredFileProviderDetector.ABSOLUTE_PATH_ISSUE,
        WeakPrngDetector.ISSUE,
        SafeBrowsingDetector.MANIFEST_ISSUE,
        PermissionDetector.PROTECTION_LEVEL_ISSUE,
        UnsafeFilenameDetector.ISSUE,
        StickyBroadcastsDetector.STICKY_METHOD_ISSUE,
        StickyBroadcastsDetector.STICKY_PERMISSION_ISSUE,
        BluetoothAdapterDetector.ZERO_BLUETOOTH_DISCOVERY_DURATION_ISSUE,
        BluetoothAdapterDetector.EXTENDED_BLUETOOTH_DISCOVERY_DURATION_ISSUE,
        LogcatDetector.LOG_INFO_DISCLOSURE_ISSUE
    )

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = 8 // works with Studio 4.1 or later; see com.android.tools.lint.detector.api.Api / ApiKt

    // Requires lint API 30.0+; if you're still building for something
    // older, just remove this property.
    override val vendor: Vendor =
        Vendor(
            vendorName = "Google - Android 3P Vulnerability Research",
            feedbackUrl = "https://github.com/google/android-security-lints/issues",
            contact = "https://github.com/google/android-security-lints"
        )
}
