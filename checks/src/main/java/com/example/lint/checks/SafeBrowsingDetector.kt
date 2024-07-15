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
import com.android.SdkConstants.ATTR_NAME
import com.android.SdkConstants.ATTR_VALUE
import com.android.SdkConstants.TAG_META_DATA
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has disabled safe browsing.
 */
class SafeBrowsingDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_META_DATA)

    override fun visitElement(context: XmlContext, element: Element) {
        val attributeName = element.getAttributeNS(ANDROID_URI, ATTR_NAME)
        val attributeValue = element.getAttributeNS(ANDROID_URI, ATTR_VALUE)

        if (attributeName == ENABLE_SAFE_BROWSING_MANIFEST_VALUE && attributeValue == "false") {
            val incident =
                Incident(
                    MANIFEST_ISSUE,
                    element,
                    context.getLocation(element),
                    "Safe Browsing should be kept enabled at all times, as it aims to keep users from unsafe URLs",
                    fix().set().android().attribute(ENABLE_SAFE_BROWSING_MANIFEST_VALUE).value("true").build()
                )

            context.report(incident)
        }
    }

    companion object {
        private const val ENABLE_SAFE_BROWSING_MANIFEST_VALUE = "android.webkit.WebView.EnableSafeBrowsing"

        private const val EXPLANATION = """
        Safe Browsing is a service to help applications check URLs against a known list of unsafe web \
        resources. We recommend keeping Safe Browsing enabled at all times and designing your app around \
        any constraints this causes.
        """

        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val MANIFEST_ISSUE: Issue =
            Issue.create(
                // ID: used in @SuppressLint warnings etc
                id = "DisabledAllSafeBrowsing",
                // Title -- shown in the IDE's preference dialog, as category headers in the
                // Analysis results window, etc
                briefDescription = "Application has disabled safe browsing for all WebView objects",
                // Full explanation of the issue; you can use some markdown markup such as
                // `monospace`, *italic*, and **bold**.
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 8,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/DisabledAllSafeBrowsing",
                implementation =
                Implementation(SafeBrowsingDetector::class.java, Scope.MANIFEST_SCOPE)
            )
    }
}
