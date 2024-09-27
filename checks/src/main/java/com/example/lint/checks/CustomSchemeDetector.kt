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
import com.android.SdkConstants.ATTR_AUTO_VERIFY
import com.android.SdkConstants.TAG_DATA
import com.android.SdkConstants.TAG_INTENT_FILTER
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.android.utils.childrenIterator
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has issues verifying custom schemes (i.e. not http/https/file/ftp/ftps).
 */
class CustomSchemeDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_INTENT_FILTER)

    override fun visitElement(context: XmlContext, element: Element) {
        val autoVerifyAttribute = element.getAttributeNS(ANDROID_URI, ATTR_AUTO_VERIFY)

        if (autoVerifyAttribute != "true") {
            val incident =
                Incident(
                    AUTOVERIFY_ATTRIBUTE_ISSUE,
                    element,
                    context.getLocation(element),
                    "Custom scheme intent filters should explicitly set the `autoVerify` attribute to true",
                    fix().set().android().attribute(ATTR_AUTO_VERIFY).value("true").build()
                )

            if (hasCustomSchemes(element)) {
                // Only have the lint check fire if there are custom schemes present
                context.report(incident)
            }
        }
    }

    private fun hasCustomSchemes(element: Element): Boolean {
        for (child in element.childrenIterator()) {
            if (child.nodeName == TAG_DATA && child.hasAttributes()) {
                for (i in 0 until child.attributes.length) {
                    val attribute = child.attributes.item(i)
                    val name = attribute.localName ?: continue
                    val value = attribute.nodeValue

                    if (value !in REGULAR_SCHEMES) {
                        return true
                    }
                }
            }
        }
        return false
    }

    companion object {
        private val REGULAR_SCHEMES = listOf("http", "https", "file", "ftp", "ftps")

        private const val EXPLANATION = """
        Intent filters should contain the `autoVerify` attribute and explicitly set it to true, in order \
        to signal to the system to automatically verify the associated hosts in your app's intent filters.
        """

        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val AUTOVERIFY_ATTRIBUTE_ISSUE: Issue =
            Issue.create(
                // ID: used in @SuppressLint warnings etc
                id = "MissingAutoVerifyAttribute",
                // Title -- shown in the IDE's preference dialog, as category headers in the
                // Analysis results window, etc
                briefDescription = "Application has custom scheme intent filters with missing `autoVerify` attributes",
                // Full explanation of the issue; you can use some markdown markup such as
                // `monospace`, *italic*, and **bold**.
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/MissingAutoVerifyAttribute",
                implementation =
                Implementation(CustomSchemeDetector::class.java, Scope.MANIFEST_SCOPE)
            )
    }
}
