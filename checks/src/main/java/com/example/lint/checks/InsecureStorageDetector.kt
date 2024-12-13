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
import com.android.SdkConstants.TAG_APPLICATION
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.android.tools.lint.detector.api.targetSdkLessThan
import org.w3c.dom.Element


class InsecureStorageDetector : Detector(), XmlScanner {
    override fun getApplicableElements() = setOf(TAG_APPLICATION)

    override fun visitElement(context: XmlContext, element: Element) {
        val attrRequestLegacyStorage =
            element.getAttributeNodeNS(ANDROID_URI, ATTR_REQUEST_LEGACY_STORAGE) ?: return

        if (attrRequestLegacyStorage.value.toBoolean()) {
            val incident =
                Incident(
                    LEGACY_EXTERNAL_STORAGE_ISSUE,
                    attrRequestLegacyStorage,
                    context.getLocation(attrRequestLegacyStorage),
                    "Setting `requestLegacyExternalStorage` to `true` will disable Android Scoped Storage",
                    fix().set().android().attribute(ATTR_REQUEST_LEGACY_STORAGE).value("false")
                        .build(),
                    )
            context.report(incident, constraint = targetSdkLessThan(PATCHED_SDK_LEVEL))
        }
    }

    companion object {
        private const val PATCHED_SDK_LEVEL = 30
        private const val ATTR_REQUEST_LEGACY_STORAGE = "requestLegacyExternalStorage"

        /** Issue describing the problem and pointing to the detector implementation. */
        @JvmField
        val LEGACY_EXTERNAL_STORAGE_ISSUE: Issue =
            Issue.create(
                id = "InsecureLegacyExternalStorage",
                briefDescription = "The `requestLegacyExternalStorage` attribute is set to true, opting the app out of scoped storage",
                explanation =
                """
                    Applications targeting Android 10 can set the `requestLegacyExternalStorage` flag to \
                    `true`, but this configuration opts the app out of Android Scoped Storage, making \
                    application-related files in external storage accessible to other applications.
                """,
                category = Category.SECURITY,
                priority = 5,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/InsecureLegacyExternalStorage",
                implementation =
                Implementation(
                    InsecureStorageDetector::class.java,
                    Scope.MANIFEST_SCOPE
                )
            )
    }
}
