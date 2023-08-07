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

import com.android.SdkConstants.*
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

/**
 * Detector flagging whether the application is vulnerable to Strandhogg / Task Affinity attacks.
 */
class StrandhoggDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_USES_SDK)

    override fun visitElement(context: XmlContext, element: Element) {
        val incident =
            Incident(
                ISSUE,
                element,
                context.getValueLocation(element.getAttributeNodeNS(ANDROID_URI, ATTR_TARGET_SDK_VERSION)),
                "Update your application's target SDK version to 28 and above to protect it from " +
                        "Strandhogg attacks"
            )

        context.report(incident, constraint = targetSdkLessThan(28))
    }

    companion object {
        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                // ID: used in @SuppressLint warnings etc
                id = "StrandhoggVulnerable",
                // Title -- shown in the IDE's preference dialog, as category headers in the
                // Analysis results window, etc
                briefDescription = "Application vulnerable to Strandhogg attacks",
                // Full explanation of the issue; you can use some markdown markup such as
                // `monospace`, *italic*, and **bold**.
                explanation =
                """
                    Apps targeting SDK versions earlier than 28 are susceptible to Strandhogg / 
                    Task Affinity attacks.
                    """, // no need to .trimIndent(), lint does that automatically
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/StrandhoggVulnerable",
                implementation =
                Implementation(StrandhoggDetector::class.java, Scope.JAVA_FILE_SCOPE)
            )
    }
}
