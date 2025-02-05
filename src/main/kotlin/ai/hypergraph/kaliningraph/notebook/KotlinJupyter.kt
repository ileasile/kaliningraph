package ai.hypergraph.kaliningraph.notebook

import ai.hypergraph.kaliningraph.*
import ai.hypergraph.kaliningraph.circuits.Gate
import ai.hypergraph.kaliningraph.matrix.BooleanMatrix
import ai.hypergraph.kaliningraph.typefamily.Graph
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

@JupyterLibrary
internal class Integration: JupyterIntegration() {
  override fun Builder.onLoaded() {
    listOf(
      "ai.hypergraph.kaliningraph.*",
      "ai.hypergraph.kaliningraph.matrix.*",
      "ai.hypergraph.kaliningraph.circuits.*",
      "org.ejml.data.*",
      "org.ejml.kotlin.*"
    ).forEach { import(it) }

    render<BooleanMatrix> { HTML("<img src=\"${it.matToImg()}\"/>") }
    render<Graph<*, *, *>> { HTML(it.html()) }
    render<Gate> { HTML(it.graph.html()) }
    render<SpsMat> { HTML("<img src=\"${it.matToImg()}\"/>") }

    // https://github.com/Kotlin/kotlin-jupyter/blob/master/docs/libraries.md#integration-using-kotlin-api
    // https://github.com/nikitinas/dataframe/blob/master/src/main/kotlin/org/jetbrains/dataframe/jupyter/Integration.kt
    // https://github.com/mipt-npm/visionforge/blob/dev/demo/jupyter-playground/src/main/kotlin/hep/dataforge/playground/VisionForgePlayGroundForJupyter.kt
  }
}