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

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_NAME
import com.android.SdkConstants.CLASS_CONTEXT
import com.android.SdkConstants.TAG_USES_PERMISSION
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getOutermostQualified
import org.jetbrains.uast.getQualifiedChain
import org.w3c.dom.Element

class StickyBroadcastsDetector : Detector(), XmlScanner, SourceCodeScanner {

    override fun getApplicableElements() = setOf(TAG_USES_PERMISSION)
    override fun getApplicableMethodNames(): List<String> = listOf(STICKY_ORDERED_METHOD)

    override fun visitElement(context: XmlContext, element: Element) {
        val attrPermissionName = element.getAttributeNS(ANDROID_URI, ATTR_NAME)
        if (attrPermissionName in DANGEROUS_PERMISSIONS) {
            val incident =
                Incident(
                    STICKY_PERMISSION_ISSUE,
                    element,
                    context.getLocation(element),
                    "Sticky broadcasts can be accessed, sent or modified by anyone. Use non-sticky broadcasts instead.",
                    fix().replace().build()
                )

            context.report(incident)
        }
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInSubClassOf(method, CLASS_CONTEXT, false)) return

        val incident =
            Incident(
                STICKY_METHOD_ISSUE,
                node,
                context.getLocation(node),
                "Sticky broadcasts can be accessed, sent or modified by anyone. Use non-sticky broadcasts instead.",
                fix().replace().build()
            )

        context.report(incident)
    }

    companion object {
        private val DANGEROUS_PERMISSIONS = setOf("android.permission.BROADCAST_STICKY")
        private const val STICKY_ORDERED_METHOD = "sendStickyOrderedBroadcast"

        private const val EXPLANATION = """
        Sticky broadcasts can be accessed, sent, or modified by anyone, resulting in potential security issues. \
        For this reason, it was deprecated in API level 21 and other mechanisms such as databases or non-sticky \
        broadcasts should be used instead.
        """

        @JvmField
        val STICKY_PERMISSION_ISSUE: Issue =
            Issue.create(
                id = "InsecureStickyBroadcastsPermission",
                briefDescription = "Usage of insecure sticky broadcasts",
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/InsecureStickyBroadcastsPermission",
                implementation = Implementation(StickyBroadcastsDetector::class.java, Scope.MANIFEST_SCOPE)
            )

        @JvmField
        val STICKY_METHOD_ISSUE: Issue =
            Issue.create(
                id = "InsecureStickyBroadcastsMethod",
                briefDescription = "Usage of insecure sticky broadcasts",
                explanation = EXPLANATION,
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "https://goo.gle/InsecureStickyBroadcastsMethod",
                implementation = Implementation(StickyBroadcastsDetector::class.java, Scope.JAVA_FILE_SCOPE)
            )
    }
}