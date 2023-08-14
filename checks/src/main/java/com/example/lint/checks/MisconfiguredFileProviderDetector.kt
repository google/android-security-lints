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

import com.android.SdkConstants
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import com.android.utils.XmlUtils
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has an improperly configured `FileProvider`.
 */
class MisconfiguredFileProviderDetector: ResourceXmlDetector() {

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun visitDocument(context: XmlContext, document: Document) {
        val root = document.documentElement ?: return

        if (root.tagName != TAG_PATHS) return

        for (child in XmlUtils.getSubTags(root)) {
            val tagName = child.tagName

            when(tagName) {
                TAG_ROOT_PATH -> handleRootPathReporting(context, child)
                TAG_EXTERNAL_PATH -> handleExternalPathReporting(context, child)
            }
        }
    }


    private fun handleRootPathReporting(context: XmlContext, element: Element) {
        val incident =
            Incident(
                ROOT_PATH_ISSUE,
                element,
                context.getElementLocation(element),
                "Do not use `<root-path>` as it provides arbitrary access to device files and folders",
                fix().replace().build()
            )
        context.report(incident)
    }

    private fun handleExternalPathReporting(context: XmlContext, element: Element) {
        val incident =
            Incident(
                EXTERNAL_PATH_ISSUE,
                element,
                context.getElementLocation(element),
                "Sensitive info like PII should not be stored or shared via `<external-path>`",
                fix().replace().build()
            )
        context.report(incident)
    }

    companion object {
        private val IMPLEMENTATION = Implementation(MisconfiguredFileProviderDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
        private const val TAG_PATHS = "paths"
        private const val TAG_ROOT_PATH = "root-path"
        private const val TAG_EXTERNAL_PATH = "external-path"

        @JvmField
        val ROOT_PATH_ISSUE: Issue =
            Issue.create(
                id = "ExposedRootPath",
                briefDescription = "Application specifies the device root directory ",
                explanation =
                """
                    Allowing the device root directory in the `FileProvider` configuration provides arbitrary access
                    to files and folders for attackers, thereby increasing the attack surface.
                    """,
                category = Category.SECURITY,
                priority = 8,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/ExposedRootPath",
                implementation = IMPLEMENTATION
            )

        @JvmField
        val EXTERNAL_PATH_ISSUE: Issue =
            Issue.create(
                id = "SensitiveExternalPath",
                briefDescription = "Application may expose sensitive info like PII by storing it in external storage",
                explanation =
                """
                    Sensitive information like PII should not be stored outside of the application container or system
                    credential storage facilities
                    """,
                category = Category.SECURITY,
                priority = 4,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/SensitiveExternalPath",
                implementation = IMPLEMENTATION
            )
    }
}