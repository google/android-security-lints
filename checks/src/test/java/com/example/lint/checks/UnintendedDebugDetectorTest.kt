/*
 * Copyright (C) 2017 The Android Open Source Project
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
class UnintendedDebugDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(UnintendedDebugDetector.ISSUE)

    override fun getDetector(): Detector = UnintendedDebugDetector()

    @Test
    fun testWhenDebuggableNotSpecified_doesNotShowWarning() {
        lint()
            .files(
                manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:minSdkVersion='30'/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenDebuggableTrue_showsWarning() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:minSdkVersion='30'/>
                    <application android:debuggable='true'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expect(
                """
                    AndroidManifest.xml:3: Warning: Setting the application's android:debuggable attribute to true exposes the application to greater risk [UnintendedDebugFlag]
                    <application android:debuggable='true'>
                                                     ~~~~
                    0 errors, 1 warnings
                    """
            )
    }

    @Test
    fun testWhenDebuggableTrue_showsQuickFix() {
        lint().files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:minSdkVersion='30'/>
                    <application android:debuggable='true'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 3: Set debuggable="false":
                    @@ -7 +7
                    -     <application android:debuggable="true" >
                    +     <application android:debuggable="false" >
                    """
        )
    }

    @Test
    fun testWhenDebuggableFalse_doesNotShowWarning() {
        lint()
            .files(
            manifest("""
                    <manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-sdk android:minSdkVersion='30'/>
                    <application android:debuggable='false'>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>
                    """
            ).indented()
        ).run().expectClean()
    }
}
