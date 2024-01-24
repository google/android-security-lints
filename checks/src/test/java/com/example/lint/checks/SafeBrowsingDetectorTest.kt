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
class SafeBrowsingDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(SafeBrowsingDetector.MANIFEST_ISSUE)

    override fun getDetector(): Detector = SafeBrowsingDetector()

    @Test
    fun testWhenNoSafeBrowsingSpecifiedInManifest_showsNoWarning() {
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
    /** Safe Browsing is enabled by default, so if no value is specified, it will be true. */
    fun testWhenSafeBrowsingEnabled_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenSafeBrowsingDisabledInManifest_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                        <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing' android:value='false'/>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
                """
                    AndroidManifest.xml:3: Warning: Safe Browsing should be kept enabled at all times, as it aims to keep users from unsafe URLs [DisabledAllSafeBrowsing]
                        <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing' android:value='false'/>
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            )
    }

    @Test
    fun testWhenSafeBrowsingDisabledInManifest_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                        <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing' android:value='false'/>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 3: Set android.webkit.WebView.EnableSafeBrowsing="true":
                    @@ -8 +8
                    +             android:android.webkit.WebView.EnableSafeBrowsing="true"
                    """
        )
    }
}
