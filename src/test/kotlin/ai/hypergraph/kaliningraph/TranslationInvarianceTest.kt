package ai.hypergraph.kaliningraph

import ai.hypergraph.kaliningraph.typefamily.*
import guru.nidi.graphviz.model.MutableGraph
import io.lacuna.bifurcan.Graph
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Edge.DEFAULT_LABEL
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.jgrapht.graph.*
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.*

class TranslationInvarianceTest {
  val randomGraph = LabeledGraph { a - b - c - d - e; a - c - e }

  @Test
  fun testTinkerpop() =
    randomGraph.let { Assertions.assertEquals(it, it.toTinkerpop().toKaliningraph()) }

  @Test
  fun testJGraphT() =
    randomGraph.let { Assertions.assertEquals(it, it.toJGraphT().toKaliningraph()) }

  @Test
  fun testGraphviz() =
    randomGraph.let { Assertions.assertEquals(it, it.render().toKaliningraph()) }

  @Test
  fun testBifurcan() =
    randomGraph.let { Assertions.assertEquals(it, it.toBifurcan().toKaliningraph()) }

  fun MutableGraph.toKaliningraph() =
    LabeledGraph {
      edges().forEach { LGVertex(it.from()?.name()?.value() ?: "") - LGVertex(it.to().name().value()) }
    }

  fun IGraph<*, *, *>.toJGraphT() =
    SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java).apply {
      vertices.forEach { addVertex(it.id) }
      edgList.forEach { (source, edge) -> addEdge(source.id, edge.target.id) }
    }

  fun <G: IGraph<G, E, V>, E: IEdge<G, E, V>, V: IVertex<G, E, V>> IGraph<G, E, V>.toBifurcan() =
    edges.fold(Graph<V, E>() as io.lacuna.bifurcan.IGraph<V, E>) { a, b -> a.link(b.source, b.target) }

  fun <V, E> io.lacuna.bifurcan.IGraph<V, E>.toKaliningraph() =
    LabeledGraph {
      edges().forEach {
        LGVertex(it.from().toString()) - LGVertex(it.to().toString())
      }
    }

  fun <E> org.jgrapht.Graph<String, E>.toKaliningraph() =
    LabeledGraph { edgeSet().map { e -> LGVertex(getEdgeSource(e)) - LGVertex(getEdgeTarget(e)) } }

  fun IGraph<*, *, *>.toTinkerpop(): GraphTraversalSource =
    TinkerGraph.open().traversal().apply {
      val map = vertices.associateWith { addV(it.id).next() }
      edgList.forEach { (v, e) ->
        (map[v] to map[e.target]).also { (t, s) ->
          addE(if (e is LabeledEdge) e.label ?: DEFAULT_LABEL else DEFAULT_LABEL).from(s).to(t).next()
        }
      }
    }

  fun GraphTraversalSource.toKaliningraph() =
    LabeledGraph {
      E().toList().forEach { LGVertex(it.inVertex().label()) - LGVertex(it.outVertex().label()) }
    }
}