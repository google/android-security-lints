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


class LogcatDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = LOGCAT_METHODS

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, LOGCAT_CLASS)) return

        val args = node.valueArguments
        if (args.isEmpty()) return

        for (element in args) {
            val transformation = ConstantEvaluator.evaluate(context, element)

            if (hasSuspiciousLogArguments(transformation.toString())) {
                val incident =
                    Incident(
                        LOG_INFO_DISCLOSURE_ISSUE,
                        node,
                        context.getLocation(node),
                        "Sensitive data should never be logged to `logcat`",
                        fix().replace().build()
                    )
                context.report(incident)
                return
            }
        }
    }

    private fun hasSuspiciousLogArguments(value: CharSequence): Boolean {
        return SUSPICIOUS_LOG_ARGUMENTS.map { it.containsMatchIn(value) }.any { it }
    }

    companion object {
        private const val LOGCAT_CLASS = "android.util.Log"
        private val LOGCAT_METHODS = listOf(
            "d",
            "e",
            "i",
            "v",
            "w",
            "wtf"
        )

        private const val EXPLANATION = """
        Sensitive information such as application secrets should never be logged to logcat. \
        Any sensitive data should therefore be removed from logcat logs. Ensure all logging \
        to logcat is sanitized in non-debug versions of your application.
        """

        private val SUSPICIOUS_LOG_ARGUMENTS = setOf(
            Regex("(?i).*pwd.*"),
            Regex("(?i).*password.*"),
            Regex("(?i).*credential.*"),
            Regex("(?i).*cred.*"),
            Regex("(?i).*key.*"),
            Regex("(?i).*secret.*"),
            Regex("(?i).*token.*"),
            Regex("(?i).*passwd.*"),
        )

        // A more resource-intensive lint check, feel free to disable after running it
        // on your application's codebase.
        @JvmField
        val LOG_INFO_DISCLOSURE_ISSUE: Issue =
            Issue.create(
                id = "LogInfoDisclosure",
                briefDescription = "Potentially sensitive information logged to Logcat",
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/LogInfoDisclosure",
                implementation = Implementation(
                    LogcatDetector::class.java,
                    Scope.JAVA_FILE_SCOPE
                )
            )
    }
}