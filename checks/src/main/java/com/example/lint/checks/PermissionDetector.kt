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
import com.android.SdkConstants.TAG_PERMISSION
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
 * Detector flagging whether there are issues with the application's permissions.
 */
class PermissionDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_PERMISSION)

    override fun visitElement(context: XmlContext, element: Element) {
        val attrProtectionLevel = element.getAttributeNS(ANDROID_URI, ATTR_PROTECTION_LEVEL)

        if (attrProtectionLevel.isEmpty() or (attrProtectionLevel in INSECURE_PROTECTION_LEVELS)) {
            val incident =
                Incident(
                    PROTECTION_LEVEL_ISSUE,
                    element,
                    context.getLocation(element),
                    "Custom permissions should have a `signature` protectionLevel or higher",
                    fix().set().android().attribute(ATTR_PROTECTION_LEVEL).value("signature").build()
                )

            context.report(incident)
        }
    }

    companion object {
        private const val ATTR_PROTECTION_LEVEL = "protectionLevel"
        // TODO: Decide whether to expand this to other protectionLevels beyond normal and/or check for flags
        private val INSECURE_PROTECTION_LEVELS = setOf("normal")

        private const val EXPLANATION = """
        Custom permissions are designed for sharing resources and capabilities with other apps. However, typos and \
        insufficient protection levels can negate the usage of these custom permissions altogether. In general, use \
        `signature` or higher protection levels whenever possible, as this ensures only other apps signed with the \
        same certificate can access these protected features.
        """

        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val PROTECTION_LEVEL_ISSUE: Issue =
            Issue.create(
                // ID: used in @SuppressLint warnings etc
                id = "InsecurePermissionProtectionLevel",
                // Title -- shown in the IDE's preference dialog, as category headers in the
                // Analysis results window, etc
                briefDescription = "Custom permission created with a normal `protectionLevel`",
                // Full explanation of the issue; you can use some markdown markup such as
                // `monospace`, *italic*, and **bold**.
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 9,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/InsecurePermissionProtectionLevel",
                implementation =
                Implementation(PermissionDetector::class.java, Scope.MANIFEST_SCOPE)
            )
    }
}
