/*
 * Copyright (C) 2024 The Android Open Source Project
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

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_TARGET_SDK_VERSION
import com.android.SdkConstants.TAG_USES_SDK
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.android.tools.lint.detector.api.targetSdkLessThan
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has insecure DNS configurations.
 */
class DnsConfigDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_USES_SDK)

    override fun visitElement(context: XmlContext, element: Element) {
        val attribute = element.getAttributeNodeNS(ANDROID_URI, ATTR_TARGET_SDK_VERSION) ?: return

        val incident =
            Incident(
                SDK_LEVEL_ISSUE,
                element,
                context.getValueLocation(attribute),
                "Update your application's target SDK version to 28 and above to make use of the Android " +
                        "OS's built-in transport security features",
                fix().set().android().attribute(ATTR_TARGET_SDK_VERSION).value(PATCHED_SDK_LEVEL.toString()).build()
            )

        context.report(incident, constraint = targetSdkLessThan(PATCHED_SDK_LEVEL))
    }

    companion object {
        const val PATCHED_SDK_LEVEL = 28

        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val SDK_LEVEL_ISSUE: Issue =
            Issue.create(
                // ID: used in @SuppressLint warnings etc
                id = "InsecureDnsSdkLevel",
                // Title -- shown in the IDE's preference dialog, as category headers in the
                // Analysis results window, etc
                briefDescription = "Application vulnerable to DNS spoofing attacks",
                // Full explanation of the issue; you can use some markdown markup such as
                // `monospace`, *italic*, and **bold**.
                explanation =
                """
                    Apps targeting SDK versions earlier than 28 are susceptible to DNS attacks like DNS spoofing or 
                    cache poisoning
                    """, // no need to .trimIndent(), lint does that automatically
                category = Category.SECURITY,
                priority = 8,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/InsecureDnsSdkLevel",
                implementation =
                Implementation(DnsConfigDetector::class.java, Scope.MANIFEST_SCOPE)
            )
    }
}
