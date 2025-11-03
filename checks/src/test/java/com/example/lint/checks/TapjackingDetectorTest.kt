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
class TapjackingDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(TapjackingDetector.ISSUE)

    override fun getDetector(): Detector = TapjackingDetector()

    @Test
    fun testWhenNamedEnableUiElement_showsWarning() {
        lint().files(
            xml(
                "res/xml/switch.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <Switch android:id='enable_setting'>
                        </Switch>
                    </LinearLayout>
                    """
            ).indented()
        ).run().expect(
            """
                    res/xml/switch.xml:3: Warning: Add the android:filterTouchesWhenObscured attribute to protect this UI element from tapjacking / overlay attacks [TapjackingVulnerable]
                        <Switch android:id='enable_setting'>
                         ~~~~~~
                    0 errors, 1 warnings
                    """
        )
    }

    @Test
    fun testWhenNamedDisableUiElement_showsWarning() {
        lint().files(
            xml(
                "res/xml/checkbox.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <CheckBox 
                        android:id='disable_setting'>
                        </CheckBox>
                    </LinearLayout>
                    """
            ).indented()
        ).run().expect(
            """
                    res/xml/checkbox.xml:3: Warning: Add the android:filterTouchesWhenObscured attribute to protect this UI element from tapjacking / overlay attacks [TapjackingVulnerable]
                        <CheckBox 
                         ~~~~~~~~
                    0 errors, 1 warnings
                    """
        )
    }

    @Test
    fun testWhenNamedEnableUiElement_showsQuickFix() {
        lint().files(
            xml(
                "res/xml/button.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <ToggleButton android:id='enable_setting'>
                        </ToggleButton>
                    </LinearLayout>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for res/xml/button.xml line 3: Set filterTouchesWhenObscured="true":
                    @@ -4 +4
                    -     <ToggleButton android:id="enable_setting" >
                    +     <ToggleButton
                    +         android:id="enable_setting"
                    +         android:filterTouchesWhenObscured="true" >
                    """
        )
    }

    @Test
    fun testWhenNamedDisableUiElement_showsQuickFix() {
        lint().files(
            xml(
                "res/xml/button.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <CompoundButton android:id='disable_setting'>
                        </CompoundButton>
                    </LinearLayout>
                    """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for res/xml/button.xml line 3: Set filterTouchesWhenObscured="true":
                    @@ -4 +4
                    -     <CompoundButton android:id="disable_setting" >
                    +     <CompoundButton
                    +         android:id="disable_setting"
                    +         android:filterTouchesWhenObscured="true" >
                    """
        )
    }

    @Test
    fun testWhenNoRelevantUiElement_noWarning() {
        lint()
            .files(
            xml(
                "res/xml/button.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <Button />
                    </LinearLayout>
                    """
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun testWhenNoInterestingNamedUiElement_noWarning() {
        lint()
            .files(
                xml(
                    "res/xml/button.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <ToggleButton></ToggleButton>
                    </LinearLayout>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenAttributeIsAlreadyPresentAndTrue_showsNoWarning() {
        lint()
            .files(
                xml(
                    "res/xml/button.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <CompoundButton
                            android:id='disable_setting'
                            android:filterTouchesWhenObscured='true' >
                        </CompoundButton>
                    </LinearLayout>
                    """
                ).indented()
            ).run().expectClean()
    }

    @Test
    fun testWhenAttributeIsAlreadyPresentAndFalse_showsQuickFix() {
        lint()
            .files(
                xml(
                    "res/xml/button.xml",
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                        <CompoundButton
                            android:id='disable_setting'
                            android:filterTouchesWhenObscured='false' >
                        </CompoundButton>
                    </LinearLayout>
                    """
                ).indented()
            ).run().expectFixDiffs(
            """
                    Fix for res/xml/button.xml line 3: Set filterTouchesWhenObscured="true":
                    @@ -6 +6
                    -         android:filterTouchesWhenObscured="false" >
                    +         android:filterTouchesWhenObscured="true" >
                    """
        )

    }
}
