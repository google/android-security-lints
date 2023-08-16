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
package com.example.lint.checks;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class WeakPrngDetectorTest : LintDetectorTest() {

    override fun getIssues() = listOf(WeakPrngDetector.ISSUE)

    override fun getDetector(): Detector = WeakPrngDetector()

    @Test
    fun testWhenMathRandomUsed_showsWarningAndQuickFix() {
        lint()
            .files(
                java(
                    """
                        package fake.pkg;
                        
                        import java.lang.Math;
                        
                        public class TestWeakPrngDetector {
                            private void foo() {
                                int random = Math.random() * 100;
                            }
                        }
                    """.trimIndent()
                )
            ).run().expect(
                """
                src/fake/pkg/TestWeakPrngDetector.java:7: Warning: Math.random relies on a weak PRNG, use java.util.Random for non-security contexts and java.security.SecureRandom for security / authentication purposes [WeakPrng]
                        int random = Math.random() * 100;
                                     ~~~~~~~~~~~~~
                0 errors, 1 warnings
                """
            ).expectFixDiffs("""
            Fix for src/fake/pkg/TestWeakPrngDetector.java line 7: Replace with Random().nextDouble():
            @@ -3 +3
            + import java.util.Random;
            @@ -7 +8
            -         int random = Math.random() * 100;
            +         int random = Random().nextDouble() * 100;
            """.trimIndent())

    }

    @Test
    fun testWhenUtilRandomUsed_showsWarningAndQuickFix() {
        lint()
            .files(
                kotlin(
                    """
                        package fake.pkg
                        
                        import java.util.Random
                        
                        class TestWeakPrngDetector {
                            private fun foo() {
                                val random = Random()
                                val randomDouble = random.nextDouble()
                            }
                        }
                    """.trimIndent()
                )
            ).run().expect(
                """
                    src/fake/pkg/TestWeakPrngDetector.kt:7: Warning: java.util.Random should only be used for non-security contexts as it is not a cryptographically secure PRNG [WeakPrng]
                            val random = Random()
                                         ~~~~~~~~
                    0 errors, 1 warnings
                    """
            ).expectFixDiffs("""
            Fix for src/fake/pkg/TestWeakPrngDetector.kt line 7: Replace with SecureRandom():
            @@ -3 +3
            + import java.security.SecureRandom
            @@ -7 +8
            -         val random = Random()
            +         val random = SecureRandom()
            """.trimIndent())
    }

    @Test
    fun testWhenNoWeakPrngUsed_noWarning() {
        lint()
            .files(
                java(
                    """
                        package fake.pkg;
                        
                        import java.security.SecureRandom;
                        
                        public class TestWeakPrngDetector {
                            private void foo() {
                                int randomInt = SecureRandom().nextInt(1000);
                            }
                        }
                    """.trimIndent()
                )
            ).run().expectClean()
    }
}
