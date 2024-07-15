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
 * Detector flagging whether the application has potentially unintended exposed URLs and private IP addresses
 * in strings.xml and Network Security Config files.
 */
class UnintendedExposedUrlDetector: ResourceXmlDetector() {

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun visitDocument(context: XmlContext, document: Document) {
        val root = document.documentElement ?: return

        if (root.tagName != TAG_NETWORK_SECURITY_CONFIG && root.tagName != SdkConstants.TAG_RESOURCES) return

        for (child in XmlUtils.getSubTags(root)) {
            val tagName = child.tagName

            if (TAG_DOMAIN_CONFIG == tagName) {
                XmlUtils.getSubTags(child).filter { TAG_DOMAIN == it.tagName }.forEach {
                    val domainName = it.textContent.trim()
                    handlePrivateIpReporting(domainName, context, it)
                    handleExposedUrlReporting(domainName, context, it)
                }
            }

            else if (SdkConstants.TAG_STRING == tagName) {
                val stringValue = child.textContent.trim()
                handlePrivateIpReporting(stringValue, context, child)
                handleExposedUrlReporting(stringValue, context, child)
            }
        }
    }

    private fun hasExposedUrl(value: String): Boolean {
        return value.matches(STAGING_URL_REGEX) ||
                value.matches(DEBUG_URL_REGEX) ||
                value.matches(PREPROD_URL_REGEX)
    }

    private fun handlePrivateIpReporting(value: String, context: XmlContext, element: Element) {
        if (!IPV4_REGEX.containsMatchIn(value)) return

        val ipAddress = IPV4_REGEX.find(value)
        val isPublicIp = PUBLIC_IP_PREFIXES.map {
            ipAddress?.value?.split(".")?.get(0) == it.toString() }.any {it}

        val incident =
            Incident(
                PRIVATE_IP_ADDRESS_ISSUE,
                element,
                context.getElementLocation(element),
                "Exposing private IP addresses puts the application and its resources at unnecessary risk",
                fix().replace().build()
            )
        if (!isPublicIp) context.report(incident)
    }

    private fun handleExposedUrlReporting(value: String, context: XmlContext, element: Element) {
        if (!hasExposedUrl(value)) return

        val incident =
            Incident(
                EXPOSED_URL_ISSUE,
                element,
                context.getElementLocation(element),
                "Exposing development / debugging URLs allows attackers to gain unintended access to the " +
                        "application and its resources",
                fix().replace().build()
            )
        context.report(incident)
    }

    companion object {
        private val IMPLEMENTATION = Implementation(UnintendedExposedUrlDetector::class.java, Scope.RESOURCE_FILE_SCOPE)

        const val TAG_NETWORK_SECURITY_CONFIG = "network-security-config"
        const val TAG_DOMAIN_CONFIG = "domain-config"
        const val TAG_DOMAIN = "domain"

        val IPV4_REGEX = Regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")
        val PUBLIC_IP_PREFIXES = setOf(10, 172, 192)

        private const val URL_PREFIX =
            "^(https:\\/\\/www\\.|http:\\/\\/www\\.|https:\\/\\/|http:\\/\\/)?\\p{Graph}*"
        private const val URL_SUFFIX = "\\p{Graph}*(.[a-zA-Z0-9]{2,})(.[a-zA-Z0-9]{2,})?"

        val STAGING_URL_REGEX = Regex("${URL_PREFIX}(staging|STAGING)${URL_SUFFIX}")
        val DEBUG_URL_REGEX = Regex("${URL_PREFIX}(debug|DEBUG)${URL_SUFFIX}")
        val PREPROD_URL_REGEX = Regex("$URL_PREFIX(preprod|PREPROD)$URL_SUFFIX")

        @JvmField
        val EXPOSED_URL_ISSUE: Issue =
            Issue.create(
                id = "UnintendedExposedUrl",
                briefDescription = "Application may have a debugging or development URL publicly exposed",
                explanation =
                """
                    URLs that look intended for debugging and development purposes only are exposed in the application, \
                    allowing attackers to gain access to parts of the application and server that should be kept secure.
                    """,
                category = Category.SECURITY,
                priority = 3,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/UnintendedExposedUrl",
                implementation = IMPLEMENTATION
            )

        @JvmField
        val PRIVATE_IP_ADDRESS_ISSUE: Issue =
            Issue.create(
                id = "UnintendedPrivateIpAddress",
                briefDescription = "Application may have a private IP address publicly exposed",
                explanation =
                """
                    Private IP addresses are referenced that may have been intended only for debugging and development. \
                    These should not be exposed publicly, as it may permit attackers to gain unintended access to the \
                    application and its resources.
                    """,
                category = Category.SECURITY,
                priority = 4,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/UnintendedPrivateIpAddress",
                implementation = IMPLEMENTATION
            )
    }
}