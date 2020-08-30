import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.creating.Expect
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.runtime.scope.ScopeId
import org.spekframework.spek2.runtime.scope.ScopeImpl
import org.spekframework.spek2.runtime.scope.ScopeType
import java.nio.file.Files.readString
import java.nio.file.Path

internal inline fun <reified S: ScopeImpl> mockScope(name: String) = mockk<S>().also {
	every { it.id } returns ScopeId(ScopeType.Scope, name)
}

internal val Expect<Path>.content get() = feature("string content") { readString(this) }
