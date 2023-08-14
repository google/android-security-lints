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
class MisconfiguredFileProviderDetectorTest : LintDetectorTest() {
    override fun getIssues() = mutableListOf(
        MisconfiguredFileProviderDetector.ROOT_PATH_ISSUE,
        MisconfiguredFileProviderDetector.EXTERNAL_PATH_ISSUE
    )

    override fun getDetector(): Detector = MisconfiguredFileProviderDetector()

    @Test
    fun testWhenRootPathUsedInConfig_showsWarningAndQuickFix() {
        lint()
            .files(
                xml("res/xml/file_paths.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <paths xmlns:android="http://schemas.android.com/apk/res/android">
                       <files-path name="my_images" path="images/"/>
                       <files-path name="my_docs" path="docs/"/>
                       <root-path name="root" path="/"/>
                    </paths>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/file_paths.xml:5: Warning: Do not use <root-path> as it provides arbitrary access to device files and folders [ExposedRootPath]
                                           <root-path name="root" path="/"/>
                                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            ).expectFixDiffs("""
                Fix for res/xml/file_paths.xml line 5: Delete:
                @@ -5 +5
                -                        <root-path name="root" path="/"/>
            """.trimIndent())
    }

    @Test
    fun testWhenExternalPathUsedInConfig_showsWarningAndQuickFix() {
        lint()
            .files(
                xml("res/xml/file_paths.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <paths xmlns:android="http://schemas.android.com/apk/res/android">
                       <files-path name="my_images" path="images/"/>
                       <files-path name="my_docs" path="docs/"/>
                       <external-path name="external_path" path="sdcard/"/>
                    </paths>
                    """
                ).indented()
            ).run().expect(
                """
                    res/xml/file_paths.xml:5: Warning: Sensitive info like PII should not be stored or shared via <external-path> [SensitiveExternalPath]
                                           <external-path name="external_path" path="sdcard/"/>
                                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            ).expectFixDiffs("""
                Fix for res/xml/file_paths.xml line 5: Delete:
                @@ -5 +5
                -                        <external-path name="external_path" path="sdcard/"/>
            """.trimIndent())
    }

    @Test
    fun testWhenNoRootOrExternalPathUsedInConfig_noWarning() {
        lint()
            .files(
                xml("res/xml/file_paths.xml",
                    """<?xml version="1.0" encoding="utf-8"?>
                    <paths xmlns:android="http://schemas.android.com/apk/res/android">
                       <files-path name="my_images" path="images/"/>
                       <files-path name="my_docs" path="docs/"/>
                    </paths>
                    """
                ).indented()
            ).run().expectClean()
    }
}
