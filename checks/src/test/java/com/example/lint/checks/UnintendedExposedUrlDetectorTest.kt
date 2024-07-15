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

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UnintendedExposedUrlDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(
        UnintendedExposedUrlDetector.EXPOSED_URL_ISSUE,
        UnintendedExposedUrlDetector.PRIVATE_IP_ADDRESS_ISSUE
    )

    override fun getDetector(): Detector = UnintendedExposedUrlDetector()

    @Test
    fun testWhenPrivateIpAddressInNetworkSecurityConfig_showsWarning() {
        lint()
            .files(
                xml("res/xml/network_security_config.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <network-security-config>
                        <base-config cleartextTrafficPermitted="false">
                        </base-config>
                        <domain-config cleartextTrafficPermitted="false">
                            <domain>http://102.1.0.4/hello</domain>
                            <domain>https://72.4.2.6</domain>
                            <domain>8.0.0.28</domain>
                        </domain-config>
                    </network-security-config>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/network_security_config.xml:6: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                                <domain>http://102.1.0.4/hello</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/network_security_config.xml:7: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                                <domain>https://72.4.2.6</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/network_security_config.xml:8: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                                <domain>8.0.0.28</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 3 warnings
                """
            ).expectFixDiffs("Fix for res/xml/network_security_config.xml line 6: Delete:\n" +
                    "@@ -6 +6\n" +
                    "-                             <domain>http://102.1.0.4/hello</domain>\n" +
                    "Fix for res/xml/network_security_config.xml line 7: Delete:\n" +
                    "@@ -7 +7\n" +
                    "-                             <domain>https://72.4.2.6</domain>\n" +
                    "Fix for res/xml/network_security_config.xml line 8: Delete:\n" +
                    "@@ -8 +8\n" +
                    "-                             <domain>8.0.0.28</domain>")
    }

    @Test
    fun testWhenPrivateIpAddressInXmlFile_showsWarning() {
        lint()
            .files(
                xml("res/xml/strings.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                        <string name='test'>http://102.1.0.4/hello</string>
                        <string name='test2'>https://72.4.2.6</string>
                        <string name='test3'>8.0.0.28</string>
                    </resources>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/strings.xml:3: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                            <string name='test'>http://102.1.0.4/hello</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/strings.xml:4: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                            <string name='test2'>https://72.4.2.6</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/strings.xml:5: Warning: Exposing private IP addresses puts the application and its resources at unnecessary risk [UnintendedPrivateIpAddress]
                                            <string name='test3'>8.0.0.28</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 3 warnings
                """
            ).expectFixDiffs("Fix for res/xml/strings.xml line 3: Delete:\n" +
                    "@@ -3 +3\n" +
                    "-                         <string name='test'>http://102.1.0.4/hello</string>\n" +
                    "Fix for res/xml/strings.xml line 4: Delete:\n" +
                    "@@ -4 +4\n" +
                    "-                         <string name='test2'>https://72.4.2.6</string>\n" +
                    "Fix for res/xml/strings.xml line 5: Delete:\n" +
                    "@@ -5 +5\n" +
                    "-                         <string name='test3'>8.0.0.28</string>")
    }

    @Test
    fun testWhenExposedUrlInNetworkSecurityConfig_showsWarning() {
        lint()
            .files(
                xml("res/xml/network_security_config.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <network-security-config>
                        <base-config cleartextTrafficPermitted="false">
                        </base-config>
                        <domain-config cleartextTrafficPermitted="false">
                            <domain>http://staging-app.com</domain>
                            <domain>https://www.fakepreprodenvt.co.uk</domain>
                            <domain>debug.io</domain>
                        </domain-config>
                    </network-security-config>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/network_security_config.xml:6: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                                <domain>http://staging-app.com</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/network_security_config.xml:7: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                                <domain>https://www.fakepreprodenvt.co.uk</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/network_security_config.xml:8: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                                <domain>debug.io</domain>
                                                ~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 3 warnings
                """
            ).expectFixDiffs("Fix for res/xml/network_security_config.xml line 6: Delete:\n" +
                    "@@ -6 +6\n" +
                    "-                             <domain>http://staging-app.com</domain>\n" +
                    "Fix for res/xml/network_security_config.xml line 7: Delete:\n" +
                    "@@ -7 +7\n" +
                    "-                             <domain>https://www.fakepreprodenvt.co.uk</domain>\n" +
                    "Fix for res/xml/network_security_config.xml line 8: Delete:\n" +
                    "@@ -8 +8\n" +
                    "-                             <domain>debug.io</domain>")
    }

    @Test
    fun testWhenExposedUrlInXmlFile_showsWarning() {
        lint()
            .files(
                xml("res/xml/strings.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                        <string name='test'>http://staging-app.com</string>
                        <string name='test2'>https://www.fakepreprodenvt.co.uk</string>
                        <string name='test3'>debug.io</string>
                    </resources>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/strings.xml:3: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                            <string name='test'>http://staging-app.com</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/strings.xml:4: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                            <string name='test2'>https://www.fakepreprodenvt.co.uk</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    res/xml/strings.xml:5: Warning: Exposing development / debugging URLs allows attackers to gain unintended access to the application and its resources [UnintendedExposedUrl]
                                            <string name='test3'>debug.io</string>
                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 3 warnings
                """
            ).expectFixDiffs("Fix for res/xml/strings.xml line 3: Delete:\n" +
                    "@@ -3 +3\n" +
                    "-                         <string name='test'>http://staging-app.com</string>\n" +
                    "Fix for res/xml/strings.xml line 4: Delete:\n" +
                    "@@ -4 +4\n" +
                    "-                         <string name='test2'>https://www.fakepreprodenvt.co.uk</string>\n" +
                    "Fix for res/xml/strings.xml line 5: Delete:\n" +
                    "@@ -5 +5\n" +
                    "-                         <string name='test3'>debug.io</string>")
    }

    @Test
    fun testWhenNoExposedUrlInXmlFile_noWarning() {
        lint()
            .files(
            xml("res/xml/strings.xml",
                """<?xml version="1.0" encoding="utf-8"?>
                    <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                        <string name='test'>stagin.com</string>
                        <string name='test2'>http://prepro.com</string>
                        <!-- We only check for all lower or all upper case sensitive words to prevent false positives -->
                        <string name='test3'>https://DeBugger.com</string>
                    </resources>
                    """
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun testWhenNoExposedUrlInNetworkConfigFile_noWarning() {
        lint()
            .files(
                xml("res/xml/network_security_config.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <network-security-config>
                        <base-config cleartextTrafficPermitted="false">
                        </base-config>
                        <domain-config cleartextTrafficPermitted="false">
                            <domain>STaGing.com</domain>
                            <domain>https://www.PreProd-hi.com</domain>
                            <domain>www.ebugd.com</domain>
                        </domain-config>
                    </network-security-config>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenNoPrivateIpAddressInXmlFile_noWarning() {
        lint()
            .files(
                xml("res/xml/strings.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                        <string name='test'>http://192.1.0.4/hello</string>
                        <string name='test2'>https://172.4.2.6</string>
                        <string name='test3'>10.0.0.28</string>
                    </resources>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenNoPrivateIpAddressInNetworkConfigFile_noWarning() {
        lint()
            .files(
                xml("res/xml/network_security_config.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <network-security-config>
                        <base-config cleartextTrafficPermitted="false">
                        </base-config>
                        <domain-config cleartextTrafficPermitted="false">
                            <domain>http://192.1.0.4/hello</domain>
                            <domain>https://172.4.2.6</domain>
                            <domain>10.0.0.28</domain>
                        </domain-config>
                    </network-security-config>
                    """
                ).indented()
            ).run().expectClean()
    }
}
