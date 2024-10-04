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

import com.android.tools.lint.checks.DataFlowAnalyzer
import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.TYPE_INT
import com.android.tools.lint.client.api.TYPE_STRING
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType

/**
 * Detector that identifies instances when the app trusts unsanitized filenames which might be
 * maliciously constructed.
 */
class UnsafeFilenameDetector : Detector(), SourceCodeScanner {

  override fun getApplicableMethodNames(): List<String> =
      listOf(METHOD_GET_COLUMN_INDEX, METHOD_GET_COLUMN_INDEX_OR_THROW)

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val parentMethod = node.getParentOfType(UMethod::class.java) ?: return
    val evaluator = context.evaluator

    // We only care about calls to methods whose only argument is of type String.
    if (!evaluator.methodMatches(method, CLASS_CURSOR, allowInherit = true, TYPE_STRING)) return

    // We require the first argument to be "_display_name".
    if (!isFirstArgDisplayName(node, context)) return

    parentMethod.accept(DisplayNameDataFlowAnalyzer(node, context, evaluator))
  }

  /**
   * Tracks `cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)` flowing into
   * `cursor.getString(tracked)`, which is then tracked further using [FilenameDataFlowAnalyzer].
   */
  private class DisplayNameDataFlowAnalyzer(
      val trackedNode: UElement,
      val context: JavaContext,
      val evaluator: JavaEvaluator,
  ) : DataFlowAnalyzer(setOf(trackedNode)) {
    override fun argument(call: UCallExpression, reference: UElement) {
      val psiMethod = call.resolve() ?: return

      if (call.methodName == METHOD_GET_STRING &&
          evaluator.methodMatches(psiMethod, CLASS_CURSOR, allowInherit = true, TYPE_INT)) {
        val parentMethod = call.getParentOfType(UMethod::class.java)
        parentMethod?.accept(FilenameDataFlowAnalyzer(call, evaluator, context))
      }
    }
  }

  /**
   * Tracks and reports cases where the `filename` String (returned by a call to
   * `cursor.getString(...)`) flows into a `File(...tracked...)` constructor.
   */
  private class FilenameDataFlowAnalyzer(
      val trackedFilename: UElement,
      val evaluator: JavaEvaluator,
      val context: JavaContext,
  ) : DataFlowAnalyzer(setOf(trackedFilename)) {
    private var isPotentiallySanitized = false

    override fun receiver(call: UCallExpression) {
      isPotentiallySanitized = true
    }

    override fun argument(call: UCallExpression, reference: UElement) {
      val psiMethod = call.resolve()

      if (psiMethod != null &&
          psiMethod.isConstructor &&
          evaluator.extendsClass(psiMethod.containingClass, CLASS_FILE) &&
          !isPotentiallySanitized) {
        context.report(
            ISSUE,
            reference,
            context.getLocation(reference),
            """
              Using `${reference.sourcePsi?.text}` is unsafe as it is a filename obtained directly \
              from a `ContentProvider`. You should sanitize it before using it for creating a \
              `File`.
          """
                .trimMargin(),
        )
      } else {
        isPotentiallySanitized = true
      }
    }
  }

  private fun isFirstArgDisplayName(node: UCallExpression, context: JavaContext): Boolean {
    val firstArg = node.valueArguments.firstOrNull() ?: return false
    val evaluatedConstant = ConstantEvaluator.evaluate(context, firstArg)
    return evaluatedConstant is String && evaluatedConstant == LITERAL_DISPLAY_NAME
  }

  companion object {
    @JvmField
    val ISSUE =
        Issue.create(
            id = "UnsanitizedContentProviderFilename",
            briefDescription = "Trusting ContentProvider filenames without any sanitization",
            explanation =
                """
                  When communicating between applications with files, the server app can provide the \
                  client app with a maliciously constructed filename. The client app should never trust \
                  this filename and should either sanitize it or completely discard it.
                """,
            moreInfo = "https://goo.gle/UnsanitizedContentProviderFilename",
            category = Category.SECURITY,
            priority = 6,
            severity = Severity.WARNING,
            androidSpecific = true,
            implementation =
                Implementation(UnsafeFilenameDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )

    private const val LITERAL_DISPLAY_NAME = "_display_name"
    private const val METHOD_GET_COLUMN_INDEX = "getColumnIndex"
    private const val METHOD_GET_COLUMN_INDEX_OR_THROW = "getColumnIndexOrThrow"
    private const val METHOD_GET_STRING = "getString"
    private const val CLASS_CURSOR = "android.database.Cursor"
    private const val CLASS_FILE = "java.io.File"
  }
}
