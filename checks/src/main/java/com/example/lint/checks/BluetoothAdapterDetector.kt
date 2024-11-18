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

class BluetoothAdapterDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(PUT_EXTRA_METHOD)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, INTENT_CLASS)) return

        val args = node.valueArguments

        if (args.isEmpty()) return
        val firstParam = ConstantEvaluator.evaluate(context, args[0])
        val secondParam = ConstantEvaluator.evaluate(context, args[1]) as? Int

        if (firstParam == "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION" && (secondParam ?: 0) == 0) {
            val incident =
                Incident(
                    ZERO_BLUETOOTH_DISCOVERY_DURATION_ISSUE,
                    node,
                    context.getLocation(node),
                    "The EXTRA_DISCOVERABLE_DURATION time should never be set to zero",
                    fix().replace().text((secondParam).toString()).with(RECOMMENDED_MAX_DISCOVERY_DURATION.toString()).build()
                )
            context.report(incident)
        }

        if (firstParam == "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION" && (secondParam ?: 0) > 120) {
            val incident =
                Incident(
                    EXTENDED_BLUETOOTH_DISCOVERY_DURATION_ISSUE,
                    node,
                    context.getLocation(node),
                    "The EXTRA_DISCOVERABLE_DURATION time should be set to a shorter amount of time",
                    fix().replace().text((secondParam).toString()).with(RECOMMENDED_MAX_DISCOVERY_DURATION.toString()).build()
                )
            context.report(incident)
        }

    }

    companion object {
        private const val INTENT_CLASS = "android.content.Intent"
        private const val PUT_EXTRA_METHOD = "putExtra"
        private const val RECOMMENDED_MAX_DISCOVERY_DURATION = 120

        @JvmField
        val ZERO_BLUETOOTH_DISCOVERY_DURATION_ISSUE: Issue =
            Issue.create(
                id = "ZeroBluetoothDiscoveryDuration",
                briefDescription = "The EXTRA_DISCOVERABLE_DURATION parameter is set to zero",
                explanation = """Setting the EXTRA_DISCOVERABLE_DURATION parameter to zero \
                    will cause the device to be discoverable as long as the application is running \
                    in the background or foreground.""",
                category = Category.SECURITY,
                priority = 9,
                severity = Severity.ERROR,
                moreInfo = "https://goo.gle/ZeroBluetoothDiscoveryDuration",
                implementation = Implementation(
                    BluetoothAdapterDetector::class.java,
                    Scope.JAVA_FILE_SCOPE
                )
            )

        @JvmField
        val EXTENDED_BLUETOOTH_DISCOVERY_DURATION_ISSUE: Issue =
            Issue.create(
                id = "ExtendedBluetoothDiscoveryDuration",
                briefDescription = "The EXTRA_DISCOVERABLE_DURATION parameter is unsafely high",
                explanation = """Setting the EXTRA_DISCOVERABLE_DURATION parameter to more than \
                    120 seconds can cause the device to be discoverable for an unsafe amount of time. \
                    This could increase the timeframe in which attackers can conduct Bluetooth attacks.""",
                category = Category.SECURITY,
                priority = 7,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/ExtendedBluetoothDiscoveryDuration",
                implementation = Implementation(
                    BluetoothAdapterDetector::class.java,
                    Scope.JAVA_FILE_SCOPE
                )
            )

    }
}