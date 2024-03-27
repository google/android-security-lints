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
class CustomSchemeDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(CustomSchemeDetector.AUTOVERIFY_ATTRIBUTE_ISSUE)

    override fun getDetector(): Detector = CustomSchemeDetector()

    @Test
    fun testWhenNoIntentFilterSpecifiedInManifest_showsNoWarning() {
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
    fun testWhenAutoVerifyAttributeSpecifiedOnCustomSchemeIntentFilter_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter android:autoVerify='true'>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='telegram://' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenNoAutoVerifyAttributeSpecifiedOnRegularSchemeIntentFilter_noWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='http' />
                                <data android:scheme='https' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenFalseAutoVerifyAttributeSpecifiedOnCustomSchemeIntentFilter_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter android:autoVerify='false'>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='telegram://' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
            """
                    AndroidManifest.xml:5: Warning: Custom scheme intent filters should explicitly set the autoVerify attribute to true [MissingAutoVerifyAttribute]
                            <intent-filter android:autoVerify='false'>
                            ^
                    0 errors, 1 warnings
                    """
        )
    }

    @Test
    fun testWhenNoAutoVerifyAttributeSpecifiedOnCustomSchemeIntentFilter_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='telegram://' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
                """
                    AndroidManifest.xml:5: Warning: Custom scheme intent filters should explicitly set the autoVerify attribute to true [MissingAutoVerifyAttribute]
                            <intent-filter>
                            ^
                    0 errors, 1 warnings
                    """
            )
    }

    @Test
    fun testWhenNoAutoVerifyAttributeSpecifiedOnCustomSchemeIntentFilter_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='telegram://' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 5: Set autoVerify="true":
                    @@ -9 +9
                    -             <intent-filter>
                    +             <intent-filter android:autoVerify="true" >
                    """
        )
    }

    @Test
    fun testWhenFalseAutoVerifyAttributeSpecifiedOnCustomSchemeIntentFilter_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <application android:debuggable='false'>
                    <meta-data android:name='android.webkit.WebView.EnableSafeBrowsing'/>
                        <activity android:name='com.example.MainActivity'>
                            <intent-filter android:autoVerify='false'>
                                <action android:name='android.intent.action.VIEW' />
                                <data android:scheme='telegram://' />
                            </intent-filter>
                        </activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 5: Set autoVerify="true":
                    @@ -9 +9
                    -             <intent-filter android:autoVerify="false" >
                    +             <intent-filter android:autoVerify="true" >
                    """
        )
    }
}
