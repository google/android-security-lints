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

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_ID
import com.android.SdkConstants.CHECK_BOX
import com.android.SdkConstants.COMPOUND_BUTTON
import com.android.SdkConstants.SWITCH
import com.android.SdkConstants.TOGGLE_BUTTON
import com.android.SdkConstants.VALUE_TRUE
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has UI elements susceptible to tapjacking / overlay attacks.
 */
class TapjackingDetector : ResourceXmlDetector() {
    // Pick out specifically UI elements with two states as it is more likely to govern whether a setting is enabled
    override fun getApplicableElements() = setOf(TOGGLE_BUTTON, COMPOUND_BUTTON, CHECK_BOX, SWITCH)

    override fun visitElement(context: XmlContext, element: Element) {
        val name = element.getAttributeNodeNS(ANDROID_URI, ATTR_ID)?.value ?: return

        if (ENABLE_NAME in name || DISABLE_NAME in name) {
            val incident =
                Incident(
                    ISSUE,
                    element,
                    context.getElementLocation(element),
                    "Add the `android:filterTouchesWhenObscured` attribute to protect this UI element from " +
                            "tapjacking / overlay attacks",
                    fix().set().android().attribute(ATTR_FILTER_TOUCHES_OBSCURED).value(VALUE_TRUE).build()
                )

            context.report(incident)
        }
    }

    companion object {
        const val ATTR_FILTER_TOUCHES_OBSCURED = "filterTouchesWhenObscured"
        val ENABLE_NAME = "enable"
        val DISABLE_NAME = "disable"

        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "TapjackingVulnerable",
                briefDescription = "Application's UI is vulnerable to tapjacking attacks",
                explanation =
                """
                    Apps with sensitive UI elements should add the `filterTouchesWithObscured` attribute
                    to protect it from tapjacking / overlay attacks.
                    """,
                category = Category.SECURITY,
                priority = 3,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/TapjackingVulnerable",
                implementation =
                Implementation(TapjackingDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
            )
    }
}
