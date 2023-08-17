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

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

/**
 * Detector flagging whether the application uses weak or unsafe cryptographic algorithms.
 */
class BadCryptographyUsageDetector: Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(GET_INSTANCE)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInSubClassOf(method, CRYPTO_CIPHER, false)) return

        val args = node.valueArguments
        // Currently only checking unsafe algorithms being passed in as an argument
        if (args.isEmpty()) return
        val transformation = ConstantEvaluator.evaluate(context, args[0])

        if (!handleVulnerableAlgorithmReporting(transformation.toString(), context, node)) {
            // If we haven't already warned about a vulnerable crypto algorithm being used
            // Check if we should warn about unsafe crypto algorithm usage
            handleUnsafeAlgorithmUsage(transformation.toString(), context, node)
        }
    }

    /**
     * Returns whether there was a reported VulnerableCryptoAlgorithm issue.
     *
     * This is needed so we don't accidentally also report that the algorithm is being used unsafely.
     */
    private fun handleVulnerableAlgorithmReporting(
        value: String, context: JavaContext, node: UCallExpression) : Boolean {
        val incident = Incident(
            VULNERABLE_ALGORITHM_ISSUE,
            node,
            context.getLocation(node),
            "Using vulnerable cryptographic algorithms puts the original input at risk of discovery"
        )

        if (VULNERABLE_BLOCK_CIPHER_ALGORITHMS.map{ value.startsWith(it) }.any{ it }) {
            incident.fix = fix().replace().range(context.getLocation(node))
                .with("Cipher.getInstance(\"$SAFE_BLOCK_CIPHER\")").build()
            context.report(incident)
            return true
        }

        if (VULNERABLE_STREAM_CIPHER_ALGORITHMS.map{ value.startsWith(it) }.any{ it }) {
            incident.fix = fix().replace().range(context.getLocation(node))
                .with("Cipher.getInstance(\"$SAFE_STREAM_CIPHER\")").build()
            context.report(incident)
            return true
        }

        return false
    }

    private fun handleUnsafeAlgorithmUsage(value: String, context: JavaContext, node: UCallExpression) {
        val setup = value.split('/')
        // If algorithm/mode/padding are not all specified, return
        if (setup.size != 3) return
        val algorithm = setup[0]
        val mode = setup[1]
        val padding = setup[2]

        val incident = Incident(
            UNSAFE_ALGORITHM_USAGE_ISSUE,
            node,
            context.getLocation(node),
            "Using insecure modes and paddings with cryptographic algorithms is unsafe and vulnerable to " +
                    "attacks"
        )

        // Unsafe Cipher Padding
        // Acceptable for RSA to use CBC cipher mode with OAEPWithSHA-256AndMGF1Padding
        if (algorithm == "RSA" && padding == "PKCS1Padding") {
            incident.fix = fix().replace().range(context.getLocation(node))
                .with("Cipher.getInstance(\"RSA/$mode/OAEPWithSHA-256AndMGF1Padding\")").build()
            context.report(incident)
        }

        // Unsafe Cipher Mode
        else if (mode == "CBC") {
            if (padding == "NoPadding") {
                // This is a more serious issue, and should be an error instead of a warning
                incident.severity = Severity.ERROR
            } else if (algorithm == "RSA" && padding.startsWith("OAEP")) {
                // This is an acceptable combination of algorithm / mode / padding and does not need to be warned
                return
            }
            incident.fix = fix().replace().range(context.getLocation(node))
                .with("Cipher.getInstance(\"$algorithm/GCM/NoPadding\")").build()
            context.report(incident)
        }
    }

    companion object {
        // Classes
        private const val CRYPTO_CIPHER = "javax.crypto.Cipher"

        // Methods
        private const val GET_INSTANCE = "getInstance"

        // Argument values
        private val VULNERABLE_STREAM_CIPHER_ALGORITHMS = setOf("RC4", "ARCFOUR")
        private const val SAFE_STREAM_CIPHER = "ChaCha20"
        private val VULNERABLE_BLOCK_CIPHER_ALGORITHMS = setOf("Blowfish", "DES", "RC2", "RC5")
        private const val SAFE_BLOCK_CIPHER = "AES/GCM/NoPadding"

        private val IMPLEMENTATION = Implementation(BadCryptographyUsageDetector::class.java, Scope.JAVA_FILE_SCOPE)

        @JvmField
        val VULNERABLE_ALGORITHM_ISSUE: Issue =
            Issue.create(
                id = "VulnerableCryptoAlgorithm",
                briefDescription = "Application uses vulnerable cryptography algorithms",
                explanation =
                """
                    Using weak or broken cryptographic hash functions may allow an attacker to reasonably determine
                    the original input or produce multiple inputs with the same hash value.
                    """,
                category = Category.SECURITY,
                priority = 9,
                severity = Severity.ERROR,
                moreInfo = "http://goo.gle/VulnerableCryptoAlgorithm",
                implementation = IMPLEMENTATION
            )

        @JvmField
        val UNSAFE_ALGORITHM_USAGE_ISSUE: Issue =
            Issue.create(
                id = "UnsafeCryptoAlgorithmUsage",
                briefDescription = "Application uses unsafe cipher modes or paddings with cryptographic algorithms",
                explanation =
                    """
                        Using unsafe cipher modes or paddings with safe cryptographic algorithms is insecure, and 
                        makes the code vulnerable to issues like padding oracle attacks.
                    """,
                category = Category.SECURITY,
                priority = 8,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/UnsafeCryptoAlgorithmUsage",
                implementation = IMPLEMENTATION
            )
    }
}