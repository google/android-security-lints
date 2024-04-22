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

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PermissionDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(PermissionDetector.PROTECTION_LEVEL_ISSUE)

    override fun getDetector(): Detector = PermissionDetector()

    @Test
    fun testWhenNoCustomPermissionsInManifest_showsNoWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenCustomPermissionSignatureProtectionLevel_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION"
                                android:protectionLevel="signature" />
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenCustomPermissionDangerousProtectionLevel_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION"
                                android:protectionLevel="dangerous" />
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    /** Custom permissions have a default normal protectionLevel. */
    fun testWhenCustomPermissionNoSpecifiedProtectionLevel_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION" />
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
            """
                    AndroidManifest.xml:2: Warning: Custom permissions should have signature `protectionLevel`s or higher [InsecurePermissionProtectionLevel]
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION" />
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
        )
    }

    @Test
    fun testWhenCustomPermissionNormalProtectionLevel_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION"
                                android:protectionLevel="normal" />
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
                """
                    AndroidManifest.xml:2: Warning: Custom permissions should have signature `protectionLevel`s or higher [InsecurePermissionProtectionLevel]
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION"
                    ^
                    0 errors, 1 warnings
                    """
            )
    }

    @Test
    fun testWhenCustomPermissionNormalProtectionLevel_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <permission android:name="com.android.example.permission.CUSTOM_PERMISSION"
                                android:protectionLevel="normal" />
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 2: Set protectionLevel="signature":
                    @@ -7 +7
                    -         android:protectionLevel="normal" />
                    +         android:protectionLevel="signature" />
                    """
        )
    }
}
