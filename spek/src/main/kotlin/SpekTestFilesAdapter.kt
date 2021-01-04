package de.joshuagleitze.testfiles.spek

import de.joshuagleitze.testfiles.DefaultTestFiles
import org.spekframework.spek2.lifecycle.ExecutionResult
import org.spekframework.spek2.lifecycle.GroupScope
import org.spekframework.spek2.lifecycle.LifecycleListener
import org.spekframework.spek2.lifecycle.Scope
import org.spekframework.spek2.lifecycle.TestScope
import org.spekframework.spek2.runtime.scope.ScopeImpl

internal class SpekTestFilesAdapter internal constructor(private val testFiles: DefaultTestFiles): LifecycleListener {
	override fun beforeExecuteGroup(group: GroupScope) = testFiles.enterScope(nameOf(group))

	override fun beforeExecuteTest(test: TestScope) = testFiles.enterScope(nameOf(test))

	override fun afterExecuteGroup(group: GroupScope, result: ExecutionResult) = testFiles.leaveScope(convert(result))

	override fun afterExecuteTest(test: TestScope, result: ExecutionResult) = testFiles.leaveScope(convert(result))

	private fun nameOf(scope: Scope) = when (scope) {
		is ScopeImpl -> scope.id.name
		is GroupScope -> "unknown group"
		is TestScope -> "unknown test"
		else -> "unknown scope"
	}

	private fun convert(result: ExecutionResult) = when (result) {
		is ExecutionResult.Success -> DefaultTestFiles.ScopeResult.Success
		is ExecutionResult.Failure -> DefaultTestFiles.ScopeResult.Failure
	}
}
