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

class BadCryptographyUsageDetector: Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(GET_INSTANCE)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInSubClassOf(method, CRYPTO_CIPHER, false)) return

        val args = node.valueArguments
        // Currently only checking unsafe algorithms being passed in as an argument
        if (args.isEmpty()) return
        val transformation = ConstantEvaluator.evaluate(context, args[0])

        for (setup in VULNERABLE_CIPHER_SETUPS) {
            if (setup == transformation) {
                val incident = Incident(
                    VULNERABLE_ALGORITHM,
                    node,
                    context.getLocation(node),
                    "Using vulnerable cryptographic algorithms puts the original input at risk of discovery",
                    fix().replace().range(context.getLocation(node)).with("Cipher.getInstance(\"ChaCha20\")").build()
                )
                context.report(incident)
            }
        }
    }

    companion object {
        // Classes
        private const val CRYPTO_CIPHER = "javax.crypto.Cipher"

        // Methods
        private const val GET_INSTANCE = "getInstance"

        // Argument values
        private val VULNERABLE_CIPHER_SETUPS = setOf("AES/CBC/PKCS5Padding", "RC4")


        @JvmField
        val VULNERABLE_ALGORITHM: Issue =
            Issue.create(
                id = "VulnerableCryptoAlgorithm",
                briefDescription = "Application uses vulnerable cryptography algorithms",
                explanation =
                """
                    Using weak or broken cryptographic hash functions may allow an attacker to reasonably determine
                    the original input or produce multiple inputs with the same hash value.
                    """,
                category = Category.SECURITY,
                priority = 8,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/VulnerableCryptoAlgorithm",
                implementation =  Implementation(BadCryptographyUsageDetector::class.java, Scope.JAVA_FILE_SCOPE)
            )
    }
}