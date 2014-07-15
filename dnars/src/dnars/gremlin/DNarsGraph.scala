package dnars.gremlin

import com.tinkerpop.gremlin.scala.ScalaGraph
import com.tinkerpop.blueprints.Graph
import dnars.base.Term
import com.tinkerpop.blueprints.Vertex
import dnars.base.Truth
import com.tinkerpop.blueprints.Edge
import dnars.base.Statement
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.blueprints.Direction

class DNarsGraph(override val graph: Graph) extends ScalaGraph(graph) {

	def getOrAddV(term: Term): Vertex = {
		val existing = v(term)
		if (existing != null)
			existing
		else {
			val added = addV(term)
			added.setProperty("term", term)
			added
		}
	}
	
	def addE(subj: Vertex, copula: String, pred: Vertex, truth: Truth): Edge = {
		val edge = subj.addEdge(copula, pred)
		edge.setProperty("truth", truth)
		edge
	}
	
	def getE(st: Statement): Option[Edge] = {
		val s = v(st.subj)
		val p = v(st.pred)
		if (s == null || p == null) // no vertex, so no edge
			None
		else {
			val subj: ScalaVertex = s
			val list = subj.outE(st.copula).as("x").inV.retain(Seq(p)).back("x").toList
			list match {
				case List() => None
				case h :: Nil => Some(h.asInstanceOf[Edge])
				case _ => throw new IllegalStateException(s"Multiple edges of the same copula for $st")
			}
		}
	}
	
	def addStatement(st: Statement): Unit = {
		getE(st) match {
			case None => 
				val subj = getOrAddV(st.subj)
				val pred = getOrAddV(st.pred)
				addE(subj, st.copula, pred, st.truth)
			case Some(edge) =>
				val truth = edge.getProperty[Truth]("truth")
				edge.setProperty("truth", truth.revision(st.truth))
		}
	}
	
	def assertStatement(st: Statement): Unit = {
		getE(st) match {
			case None =>
				throw new IllegalArgumentException("Not found.");
			case Some(edge) =>
				val truth = edge.getProperty[Truth]("truth")
				if (!truth.closeTo(st.truth))
					throw new IllegalArgumentException(truth.toString)
		}
	}
	
	/**
	 * For testing purposes only. Returns all statements in the graph.
	 */
	def getAllStatements: Array[Statement] = {
		val edges = E.toList
		val n = edges.size
		val st = new Array[Statement](n)
		var i = 0
		for (e <- edges) {
			val s = e.getVertex(Direction.OUT).getProperty[Term]("term")
			val p = e.getVertex(Direction.IN).getProperty[Term]("term")
			val truth = e.getProperty[Truth]("truth")
			val copula = e.getLabel()
			st(i) = Statement(s, copula, p, truth)
			i += 1
		}
		st
	}
	
	def printEdges() {
		val list = E.map { e => e.toString + " " + e.getProperty("truth") }.toList
		for (e <- list)
			println(e)
	}
	
	def shutdown() = graph.shutdown()
}

object DNarsGraph {
	def apply(graph: ScalaGraph) = wrap(graph)
	implicit def wrap(graph: ScalaGraph) = new DNarsGraph(graph)
	implicit def unwrap(wrapper: DNarsGraph) = wrapper.graph
}