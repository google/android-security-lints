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
class StickyBroadcastsDetectorTest : LintDetectorTest() {
    override fun getIssues() =
        mutableListOf(StickyBroadcastsDetector.STICKY_PERMISSION_ISSUE, StickyBroadcastsDetector.STICKY_METHOD_ISSUE)

    override fun getDetector(): Detector = StickyBroadcastsDetector()

    @Test
    fun stickyBroadcastPermissionInManifest_showsWarning() {
        lint().files(
            manifest(
                """<manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>"""
            ).indented()
        ).run().expect(
            """
            AndroidManifest.xml:2: Warning: Sticky broadcasts can be accessed, sent or modified by anyone. Use non-sticky broadcasts instead. [InsecureStickyBroadcastsPermission]
                                <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warnings
            """
        )
    }

    @Test
    fun stickyBroadcastPermissionInManifest_showsQuickFix() {
        lint().files(
            manifest(
                """<manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>"""
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for AndroidManifest.xml line 2: Delete:
                    @@ -2 +2
                    -                     <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
                    """
        )
    }

    @Test
    fun differentBroadcastPermissionInManifest_showsNoWarning() {
        lint().files(
            manifest(
                """<manifest xmlns:android='http://schemas.android.com/apk/res/android' package='test.pkg'>
                    <uses-permission android:name="android.permission.BROADCAST_SMS"/>
                    <application>
                        <activity android:name='com.example.MainActivity'></activity>
                    </application>
                    </manifest>"""
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun stickyBroadcastMethodCall_showsWarning() {
        lint().files(
            java(
                """
                        package fake.pkg;
                        
                        import android.app.Activity;
                        import android.os.Bundle;
                        
                        @Suppress("DEPRECATION")
                        public class MainActivity extends Activity {
                            
                            @Override
                             protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                sendStickyOrderedBroadcast();
                                }
                        }"""
            ).indented()
        ).run().expect(
            """
            src/fake/pkg/MainActivity.java:12: Warning: Sticky broadcasts can be accessed, sent or modified by anyone. Use non-sticky broadcasts instead. [InsecureStickyBroadcastsMethod]
                    sendStickyOrderedBroadcast();
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warnings
            """
        )
    }

    @Test
    fun stickyBroadcastMethodCall_showsQuickFix() {
        lint().files(
            java(
                """
                        package fake.pkg;
                        
                        import android.app.Activity;
                        import android.os.Bundle;
                        
                        @Suppress("DEPRECATION")
                        public class MainActivity extends Activity {
                            
                            @Override
                             protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                Intent intent = new Intent();
                                sendStickyOrderedBroadcast(intent);
                            }
                        }"""
            ).indented()
        ).run().expectFixDiffs(
            // Unfortunately, the semi-colon will be left behind due to limitations with the lint API
            // needing to also account for chained calls, and it being impossible to fully detect the
            // entire relevant code block to delete.
            """
                    Fix for src/fake/pkg/MainActivity.java line 13: Delete:
                    @@ -13 +13
                    -         sendStickyOrderedBroadcast(intent);
                    +         ;
                    """
        )
    }

    @Test
    fun regularBroadcastMethodCall_showsNoWarning() {
        lint().files(
            java(
                """
                        package fake.pkg;
                        
                        import android.app.Activity;
                        import android.os.Bundle;
                        
                        @Suppress("DEPRECATION")
                        public class MainActivity extends Activity {
                            
                            @Override
                             protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                sendBroadcast();
                            }
                        }"""
            ).indented()
        ).run().expectClean()
    }
}