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
import com.android.SdkConstants.ANDROID_URI
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.android.tools.lint.detector.api.targetSdkLessThan
import java.io.File
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has default insecure network connections behavior by forgetting to include
 * a network security config file.
 */
class MissingNetworkSecurityConfigDetector : ResourceXmlDetector(), XmlScanner {
    override fun getApplicableElements() = setOf(SdkConstants.TAG_APPLICATION)
    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun visitElement(context: XmlContext, element: Element) {
        val networkSecurityConfigAttr = element.getAttributeNodeNS(ANDROID_URI, ATTR_NETWORK_SECURITY_CONFIG)
        // NetworkSecurityConfig file is already specified in the Manifest, no need to add one
        if (networkSecurityConfigAttr != null) return

        val cleartextTrafficIncident =
            Incident(
                CLEARTEXT_TRAFFIC_ISSUE,
                element,
                context.getElementLocation(element),
                "On SDK versions below 28, the application by default trusts cleartext traffic. Add a " +
                        "Network Security Config file to opt out of these insecure connections.",
                createNetworkSecurityConfigFix(context, CLEARTEXT_TRAFFIC_CONFIG)
            )

        val userCertsIncident =
            Incident(
                TRUSTED_USER_CERTS_ISSUE,
                element,
                context.getElementLocation(element),
                "On SDK versions below 24, the application by default trusts user-added CA certificates. Add " +
                        "a Network Security Config file to opt out of this insecure behavior.",
                createNetworkSecurityConfigFix(context, USER_CERTS_CONFIG)
            )

        val target = context.project.targetSdk
        // Check the Target SDK version so we don't accidentally display two lint warnings at the same time
        if (target in USER_CERTS_SDK_LEVEL until CLEARTEXT_TRAFFIC_SDK_LEVEL) {
            context.report(cleartextTrafficIncident, targetSdkLessThan(CLEARTEXT_TRAFFIC_SDK_LEVEL))
        } else if (target < USER_CERTS_SDK_LEVEL) {
            context.report(userCertsIncident, targetSdkLessThan(USER_CERTS_SDK_LEVEL))
        }
    }

    private fun createNetworkSecurityConfigFix(context: Context, config: String): LintFix? {
        val addConfigToManifestFix = fix().set().android().attribute(ATTR_NETWORK_SECURITY_CONFIG).value(NETWORK_SECURITY_CONFIG_LOCATION).build()
        val project = context.project
        val folder = project.resourceFolders.firstOrNull() ?: return null
        val file = File(folder, "xml/$NETWORK_SECURITY_CONFIG_FILE_NAME.xml")

        // NetworkSecurityConfig already exists, no need to create one
        if (file.exists()) return null

        val createConfigFileFix = fix().newFile(file, config).build()
        return fix().name("Create network security config").composite(addConfigToManifestFix, createConfigFileFix)
    }

    companion object {
        const val ATTR_NETWORK_SECURITY_CONFIG = "networkSecurityConfig"
        const val NETWORK_SECURITY_CONFIG_FILE_NAME = "network_security_config"
        const val NETWORK_SECURITY_CONFIG_LOCATION = "@xml/$NETWORK_SECURITY_CONFIG_FILE_NAME"
        const val CLEARTEXT_TRAFFIC_SDK_LEVEL = 28
        const val USER_CERTS_SDK_LEVEL = 24
        val CLEARTEXT_TRAFFIC_CONFIG =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <network-security-config>
                <base-config cleartextTrafficPermitted="false" />
            </network-security-config>
            """.trimIndent()
        val USER_CERTS_CONFIG =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <network-security-config>
                <base-config cleartextTrafficPermitted="false">
                    <trust-anchors>
                        <certificates src="system" />
                    </trust-anchors>
                </base-config>
            </network-security-config>
            """.trimIndent()

        @JvmField
        val IMPLEMENTATION =
            Implementation(MissingNetworkSecurityConfigDetector::class.java, Scope.MANIFEST_SCOPE)

        @JvmField
        val CLEARTEXT_TRAFFIC_ISSUE: Issue =
            Issue.create(
                id = "DefaultCleartextTraffic",
                briefDescription = "Application by default permits cleartext traffic",
                explanation =
                """
            Apps targeting SDK versions earlier than 28 trust cleartext traffic by default. \
            The application must explicitly opt out of this in order to only use secure connections.
            """,
                category = Category.SECURITY,
                priority = 5,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/DefaultCleartextTraffic",
                implementation = IMPLEMENTATION
            )

        @JvmField
        val TRUSTED_USER_CERTS_ISSUE: Issue =
            Issue.create(
                id = "DefaultTrustedUserCerts",
                briefDescription = "Application by default trusts user-added CA certificates",
                explanation =
                """
                Apps targeting SDK versions earlier than 24 trust user-added CA certificates by default. \
                In practice, it is better to limit the set of trusted CAs so only trusted CAs are used for an app's secure \
                connections.
                """,
                category = Category.SECURITY,
                priority = 3,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/DefaultTrustedUserCerts",
                implementation = IMPLEMENTATION
            )
    }
}