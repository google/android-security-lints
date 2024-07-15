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
 * Detector flagging whether the application uses a weak PRNG (pseudorandom number generator).
 */
class WeakPrngDetector: Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(RANDOM)

    override fun getApplicableConstructorTypes(): List<String> = listOf(UTIL_RANDOM)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        // If static java.lang.Math.random method is called
        if (!context.evaluator.isMemberInSubClassOf(method, LANG_MATH, false)) return

        val incident = Incident(
            ISSUE,
            node,
            context.getLocation(node),
            "Math.random relies on a weak PRNG, use `$UTIL_RANDOM` for non-security contexts and " +
            "`$SECURE_RANDOM` for security / authentication purposes",
            fix().replace().range(context.getLocation(node)).with("Random().nextDouble()")
                .imports(UTIL_RANDOM).build()
        )
        context.report(incident)
    }

    override fun visitConstructor(context: JavaContext, node: UCallExpression, constructor: PsiMethod) {
        // If java.util.Random constructor is called
        val incident = Incident(
            ISSUE,
            node,
            context.getLocation(node),
            "`$UTIL_RANDOM` should only be used for non-security contexts as it is not a cryptographically " +
                    "secure PRNG",
            fix().replace().range(context.getLocation(node)).with("SecureRandom()").imports(
                SECURE_RANDOM).build()
        )
        context.report(incident)
    }

    companion object {
        // Classes
        private const val LANG_MATH = "java.lang.Math"
        private const val UTIL_RANDOM = "java.util.Random"
        private const val SECURE_RANDOM = "java.security.SecureRandom"

        // Methods
        private const val RANDOM = "random"

        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "WeakPrng",
                briefDescription = "Application uses non-cryptographically secure pseudorandom number generators",
                explanation =
                """
                    If a non-cryptographically secure pseudorandom number generator (PRNG) is used in a security context \
                    like authentication, an attacker may be able to guess the randomly-generated numbers and gain access \
                    to privileged data or functionality.
                    """,
                category = Category.SECURITY,
                priority = 4,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/WeakPrng",
                implementation =  Implementation(WeakPrngDetector::class.java, Scope.JAVA_FILE_SCOPE)
            )
    }
}