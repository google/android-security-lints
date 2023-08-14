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
class MissingNetworkSecurityConfigDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(
        MissingNetworkSecurityConfigDetector.CLEARTEXT_TRAFFIC_ISSUE,
        MissingNetworkSecurityConfigDetector.TRUSTED_USER_CERTS_ISSUE
    )

    override fun getDetector(): Detector = MissingNetworkSecurityConfigDetector()

    @Test
    fun testWhenNoNetworkSecurityConfig_sdkLevelAboveDefaultCleartextTraffic_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='30'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenHasNetworkSecurityConfig_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='27'/>
                    <application android:networkSecurityConfig='@xml/network_security_config'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenNoNetworkSecurityConfig_defaultCleartextTrafficSdkLevel_showsWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='27'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expect(
                """
                    AndroidManifest.xml:3: Warning: On SDK versions below 28, the application by default trusts cleartext traffic. Add a Network Security Config file to opt out of these insecure connections. [DefaultCleartextTraffic]
                    <application>
                     ~~~~~~~~~~~
                    0 errors, 1 warnings
                """
            )
    }

    @Test
    fun testWhenNoNetworkSecurityConfig_defaultUserCerts_showsWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='23'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expect(
                """
                    AndroidManifest.xml:3: Warning: On SDK versions below 24, the application by default trusts user-added CA certificates. Add a Network Security Config file to opt out of this insecure behavior. [DefaultTrustedUserCerts]
                    <application>
                     ~~~~~~~~~~~
                    0 errors, 1 warnings
                """
            )
    }

    @Test
    fun testWhenNoNetworkSecurityConfig_defaultCleartextTrafficSdkLevel_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='27'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented(),
            emptyXmlFile
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 3: Set networkSecurityConfig="@xml/network_security_config":
                    @@ -7 +7
                    -     <application>
                    +     <application android:networkSecurityConfig="@xml/network_security_config" >
                    res/xml/network_security_config.xml:
                    @@ -1 +1
                    + <?xml version="1.0" encoding="utf-8"?>
                    + <network-security-config>
                    +     <base-config cleartextTrafficPermitted="false" />
                    + </network-security-config>
                    """
        )
    }

    @Test
    fun testWhenNoNetworkSecurityConfig_defaultUserCerts_showsQuickFix() {
        // TODO: fix why quick fix is re
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='23'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented(),
            emptyXmlFile
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 3: Set networkSecurityConfig="@xml/network_security_config":
                    @@ -7 +7
                    -     <application>
                    +     <application android:networkSecurityConfig="@xml/network_security_config" >
                    res/xml/network_security_config.xml:
                    @@ -1 +1
                    + <?xml version="1.0" encoding="utf-8"?>
                    + <network-security-config>
                    +     <base-config cleartextTrafficPermitted="false">
                    +         <trust-anchors>
                    +             <certificates src="system" />
                    +         </trust-anchors>
                    +     </base-config>
                    + </network-security-config>
                    """
        )
    }

    companion object {
        // It's necessary to add an empty XML File in order for the unit test to recognize that a resources folder
        // exists
        val emptyXmlFile = xml(
            "res/xml/strings.xml",
            """
                <strings></strings>
                """
        )
    }
}
