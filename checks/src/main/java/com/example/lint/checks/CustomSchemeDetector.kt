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
import com.android.SdkConstants.ATTR_SCHEME
import com.android.SdkConstants.TAG_ACTION
import com.android.SdkConstants.TAG_CATEGORY
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

class CustomSchemeDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_INTENT_FILTER)

    override fun visitElement(context: XmlContext, element: Element) {
        if (!hasViewAction(element) ||
            !hasBrowsableCategory(element) ||
            !hasDefaultCategory(element)
        ) {
            return
        }

        if (element.hasAttributeNS(ANDROID_URI, ATTR_AUTO_VERIFY)) {
            return
        }

        val dataElements = element.getElementsByTagName(TAG_DATA)
        if (dataElements.length == 0) {
            return
        }

        for (i in 0 until dataElements.length) {
            val data = dataElements.item(i) as Element
            if (!data.hasAttributeNS(ANDROID_URI, ATTR_SCHEME)) {
                continue
            }

            val scheme = data.getAttributeNS(ANDROID_URI, ATTR_SCHEME)
            if (scheme == "http" || scheme == "https") {
                context.report(
                    AUTOVERIFY_ATTRIBUTE_ISSUE,
                    element,
                    context.getNameLocation(element),
                    "This intent filter matches App Links (VIEW, BROWSABLE, DEFAULT, http/https), " +
                    "but is missing the `android:autoVerify=\"true\"` attribute. " +
                    "See https://developer.android.com/training/app-links/verify-android-applinks",
                    fix().set().namespace(ANDROID_URI).attribute(ATTR_AUTO_VERIFY).value("true").build()
                )
                return
            }
        }
    }

    private fun hasViewAction(element: Element): Boolean {
        val actions = element.getElementsByTagName(TAG_ACTION)
        for (i in 0 until actions.length) {
            val action = actions.item(i) as Element
            if ("android.intent.action.VIEW" == action.getAttributeNS(ANDROID_URI, "name")) {
                return true
            }
        }
        return false
    }

    private fun hasBrowsableCategory(element: Element): Boolean {
        val categories = element.getElementsByTagName(TAG_CATEGORY)
        for (i in 0 until categories.length) {
            val category = categories.item(i) as Element
            if ("android.intent.category.BROWSABLE" == category.getAttributeNS(ANDROID_URI, "name")) {
                return true
            }
        }
        return false
    }

    private fun hasDefaultCategory(element: Element): Boolean {
        val categories = element.getElementsByTagName(TAG_CATEGORY)
        for (i in 0 until categories.length) {
            val category = categories.item(i) as Element
            if ("android.intent.category.DEFAULT" == category.getAttributeNS(ANDROID_URI, "name")) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val EXPLANATION = """
            Intent filters that handle `http` or `https` schemes and include \
            `android.intent.action.VIEW`, `android.intent.category.BROWSABLE`, and \
            `android.intent.category.DEFAULT` should also include \
            `android:autoVerify="true"`. This enables Android App Links, which securely \
            associates your app with your domain. \
            See https://developer.android.com/training/app-links/verify-android-applinks
            """

        @JvmField
        val AUTOVERIFY_ATTRIBUTE_ISSUE: Issue =
            Issue.create(
                id = "MissingAutoVerifyAttribute",
                briefDescription = "Application has http/https scheme intent filters with missing `autoVerify` attributes",
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
