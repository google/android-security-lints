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
class StrandhoggDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(StrandhoggDetector.ISSUE)

    override fun getDetector(): Detector = StrandhoggDetector()

    @Test
    fun testWhenNoTargetSdkVersionSpecified_showsNoWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:minSdkVersion='27'/>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenTargetSdkBelowStrandhoggPatch_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='27'/>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
                """
                    AndroidManifest.xml:2: Warning: Update your application's target SDK version to 28 and above to protect it from Strandhogg attacks [StrandhoggVulnerable]
                    <uses-sdk android:targetSdkVersion='27'/>
                                                        ~~
                    0 errors, 1 warnings
                    """
            )
    }

    @Test
    fun testWhenTargetSdkBelowStrandhoggPatch_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='27'/>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 2: Set targetSdkVersion="28":
                    @@ -5 +5
                    -     <uses-sdk android:targetSdkVersion="27" />
                    +     <uses-sdk android:targetSdkVersion="28" />
                    """
        )
    }

    @Test
    fun testWhenTargetSdkAtStrandhoggPatch_noWarning() {
        lint()
            .files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:targetSdkVersion='28'/>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectClean()
    }
}
