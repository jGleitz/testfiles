package de.joshuagleitze.testfiles.kotest

import de.joshuagleitze.testfiles.DefaultTestFiles
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.AutoScan
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestStatus.Error
import io.kotest.core.test.TestStatus.Failure
import io.kotest.core.test.TestStatus.Ignored
import io.kotest.core.test.TestStatus.Success
import kotlin.reflect.KClass

@AutoScan
internal object KotestTestFilesAdapter: TestListener {
	override val name: String get() = "testfiles"

	override suspend fun prepareSpec(kclass: KClass<out Spec>) {
		internalTestFiles.enterScope(kclass.qualifiedName ?: "<anonymous spec>")
	}

	override suspend fun finalizeSpec(kclass: KClass<out Spec>, results: Map<TestCase, TestResult>) {
		internalTestFiles.leaveScope(results.values.map { convert(it) }.reduce { left, right -> left.combineWith(right) })
	}

	override suspend fun beforeAny(testCase: TestCase) {
		internalTestFiles.enterScope(testCase.displayName)
	}

	override suspend fun afterAny(testCase: TestCase, result: TestResult) {
		internalTestFiles.leaveScope(convert(result))
	}

	private fun convert(result: TestResult) = when (result.status) {
		Success -> DefaultTestFiles.ScopeResult.Success
		Error, Failure -> DefaultTestFiles.ScopeResult.Failure
		Ignored -> error("contact breach: kotest should not have called us!")
	}
}
