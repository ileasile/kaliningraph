package edu.mcgill.kaliningraph.notebook

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import jupyter.kotlin.DependsOn
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.*
import org.jetbrains.kotlinx.jupyter.api.*
import org.jetbrains.kotlinx.jupyter.libraries.EmptyResolutionInfoProvider
import org.junit.Before
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.script.experimental.jvm.util.classpathFromClassloader


class RenderingTests: AbstractReplTest() {
  @Test
  fun `graph is rendered to html`() {
    @Language("kts")
    val html = execHtml(
      """
            @file:Repository("https://jitpack.io")
            @file:DependsOn("com.github.breandan:kaliningraph:0.1.7")

            import edu.mcgill.kaliningraph.*
            import edu.mcgill.kaliningraph.matrix.*
            import edu.mcgill.kaliningraph.circuits.*
            import org.ejml.data.*
            import org.ejml.kotlin.*

            val g1 = LabeledGraph { a - b - c - a }; g1
        """.trimIndent()
    )
    html shouldContain "polygon"
  }
}

// https://github.com/Kotlin/kotlin-jupyter/issues/270
abstract class AbstractReplTest {
  private val repl: ReplForJupyter = ReplForJupyterImpl(
    EmptyResolutionInfoProvider, classpath, isEmbedded = true)

  @Before
  fun initRepl() {
    // Jupyter integration is loaded after some code was executed, so we do it here
    // We also define here a class to retrieve values without rendering
    exec("class $WRAP(val $WRAP_VAL: Any?)")
  }

  fun exec(code: Code): Any? {
    return repl.eval(code).resultValue
  }

  @JvmName("execTyped")
  inline fun <reified T: Any> exec(code: Code): T {
    val res = exec(code)
    res.shouldBeInstanceOf<T>()
    return res
  }

  fun execHtml(code: Code): String {
    val res = exec<MimeTypedResult>(code)
    val html = res["text/html"]
    html.shouldNotBeNull()
    return html
  }

  companion object {
    @JvmStatic
    protected val WRAP = "W"

    private const val WRAP_VAL = "v"

    private val classpath = run {
      val scriptArtifacts = setOf(
        "kotlin-jupyter-lib",
        "kotlin-jupyter-api",
        "kotlin-jupyter-shared-compiler",
        "kotlin-stdlib",
        "kotlin-reflect",
        "kotlin-script-runtime",
      )
      classpathFromClassloader(DependsOn::class.java.classLoader).orEmpty().filter { file ->
        val name = file.name
        (name == "main" && file.parentFile.name == "kotlin")
          || (file.extension == "jar" && scriptArtifacts.any { name.startsWith(it) })
      }
    }
  }
}