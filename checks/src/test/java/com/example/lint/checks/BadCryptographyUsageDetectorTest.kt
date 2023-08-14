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
class BadCryptographyUsageDetectorTest : LintDetectorTest() {

    override fun getIssues() = listOf(BadCryptographyUsageDetector.VULNERABLE_ALGORITHM_ISSUE)

    override fun getDetector(): Detector = BadCryptographyUsageDetector()

    @Test
    fun testWhenVulnerableCryptoAlgoUsed_java_showsWarningAndQuickFix() {
        lint()
            .files(
                java(
                    """
                        package fake.pkg;
                        
                        import javax.crypto.Cipher;
                        
                        public class TestBadCryptoDetector {
                            private void foo() {
                                String algo = "RC4";
                                Cipher.getInstance(algo);
                            }
                        }
                    """.trimIndent()
                )
            ).run().expect(
                """
                src/fake/pkg/TestBadCryptoDetector.java:8: Warning: Using vulnerable cryptographic algorithms puts the original input at risk of discovery [VulnerableCryptoAlgorithm]
                        Cipher.getInstance(algo);
                        ~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """
            ).expectFixDiffs("""
                Fix for src/fake/pkg/TestBadCryptoDetector.java line 8: Replace with Cipher.getInstance("ChaCha20"):
                @@ -8 +8
                -         Cipher.getInstance(algo);
                +         Cipher.getInstance("ChaCha20");
            """.trimIndent())

    }

    @Test
    fun testWhenVulnerableCryptoAlgoUsed_kotlin_showsWarningAndQuickFix() {
        lint()
            .files(
                kotlin(
                    """
                        package fake.pkg
                        
                        import javax.crypto.Cipher
                        
                        class TestBadCryptoDetector {
                            private fun foo() {
                                val algo = "AES"
                                val mode = "CBC"
                                val padding = "PKCS5Padding"
                                // There are problems with variables not being recognized with a string template
                                Cipher.getInstance(algo + "/" + mode + "/" + padding)
                            }
                        }
                    """.trimIndent()
                )
            ).run().expect(
                """
                    src/fake/pkg/TestBadCryptoDetector.kt:11: Warning: Using vulnerable cryptographic algorithms puts the original input at risk of discovery [VulnerableCryptoAlgorithm]
                            Cipher.getInstance(algo + "/" + mode + "/" + padding)
                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            ).expectFixDiffs("""
                Fix for src/fake/pkg/TestBadCryptoDetector.kt line 11: Replace with Cipher.getInstance("ChaCha20"):
                @@ -11 +11
                -         Cipher.getInstance(algo + "/" + mode + "/" + padding)
                +         Cipher.getInstance("ChaCha20")
            """.trimIndent())
    }

    @Test
    fun testWhenNoVulnerableCryptoAlgoUsed_java_noWarning() {
        lint()
            .files(
                java(
                    """
                        package fake.pkg;
                        
                        import javax.crypto.Cipher;
                        
                        public class TestBadCryptoDetector {
                            private void foo() {
                                String algo = "RSA";
                                Cipher.getInstance(algo);
                            }
                        }
                    """.trimIndent()
                )
            ).run().expectClean()
    }

    @Test
    fun testWhenNoVulnerableCryptoAlgoUsed_kotlin_noWarning() {
        lint()
            .files(
                kotlin(
                    """
                        package fake.pkg
                        
                        import javax.crypto.Cipher
                        
                        class TestBadCryptoDetector {
                            private fun foo() {
                                val algo = "ChaCha20"
                                Cipher.getInstance(algo)
                            }
                        }
                    """.trimIndent()
                )
            ).run().expectClean()

    }
}
