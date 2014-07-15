package dnars.inference

import com.tinkerpop.gremlin.scala.ScalaGraph
import dnars.base.Statement
import dnars.base.Copula._
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.blueprints.Direction
import dnars.base.Truth
import dnars.base.Term
import dnars.gremlin.DNarsGraph

/**
 * Syllogistic forward inference rules. The following table represents the summary
 * of supported premises and conclusions (up to NAL-5).
 *
 *       | S->M                | S<->M     | M->S
 * ------|---------------------|-----------|--------------------
 * M->P  | S->P ded            | S->P ana  | S->P ind, S<->P cmp
 * -----------------------------------------------------------
 * M<->P | S->P ana'           | S<->P res | P->S ana'
 * -----------------------------------------------------------
 * P->M  | S->P abd, S<->P cmp | P->S ana  | --
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object ForwardInference {
	def apply(graph: DNarsGraph, statements: Seq[Statement]): Unit = {
		for (st <- statements) {
			graph.addStatement(st)
			deduction_analogy(graph, st)
			analogy_resemblance(graph, st)
			abduction_comparison_analogy(graph, st)
			induction_comparison(graph, st)
			analogy_inv(graph, st)
		}
	}
	
	// in the following functions, the first premise is taken from the graph,
	// while the second premise is passed as a parameter
	
	// M -> P  
	//		S -> M	=> S -> P ded 
	//		S ~ M	=> S -> P ana
	def deduction_analogy(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.outE(Inherit).toList
		for (e <- edges) {
			val p = e.getVertex(Direction.IN).getProperty[Term]("term")
			if (st.subj != p) {
				val truth = e.getProperty[Truth]("truth")
				val newTruth = 
					if (st.copula == Inherit) 
						truth.deduction(st.truth) 
					else 
						truth.analogy(st.truth, false)
				graph.addStatement(Statement(st.subj, Inherit, p, newTruth))
			}
		}
	}
	
	// M ~ P ::
	//		S -> M	=> S -> P ana'
	//		S ~ M	=> S ~ P res
	def analogy_resemblance(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.outE(Similar).toList
		for (e <- edges) {
			val p = e.getVertex(Direction.IN).getProperty[Term]("term")
			if (st.subj != p) {
				val truth = e.getProperty[Truth]("truth")
				if (st.copula == Inherit)
					graph.addStatement(Statement(st.subj, Inherit, p, truth.analogy(st.truth, true)))
				else
					graph.addStatement(Statement(st.subj, Similar, p, truth.resemblance(st.truth)))
			}
		}
	}	
	
	// P -> M 
	//		S -> M	=> S -> P abd, S ~ P cmp
	//		S ~ M 	=> P -> S ana
	def abduction_comparison_analogy(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.inE(Inherit).toList
		for (e <- edges) {
			val p = e.getVertex(Direction.OUT).getProperty[Term]("term")
			if (st.subj != p) {
				val truth = e.getProperty[Truth]("truth")
				if (st.copula == Inherit) {
					println(s"$p -> $m, " + st.subj + s" -> $m => " + st.subj + s" -> $p abd, ~ $p cmp")
					val abd = truth.abduction(st.truth)
					graph.addStatement(Statement(st.subj, Inherit, p, abd))
					val cmp = truth.comparison(st.truth)
					graph.addStatement(Statement(st.subj, Similar, p, cmp))
				}
				else {
					println(s"$p -> $m, " + st.subj + s" ~ $m => " + s"$p -> " + st.subj + " ana")
					val ana = truth.analogy(st.truth, false)
					graph.addStatement(Statement(p, Inherit, st.subj, ana))
				}
			}
		}
	}
	
	// M -> P, M -> S => S -> P ind, S ~ P cmp
	def induction_comparison(graph: DNarsGraph, st: Statement): Unit = {
		if (st.copula == Inherit) {
			val m: ScalaVertex = graph.v(st.subj)
			val edges = m.outE(Inherit).toList
			for (e <- edges) {
				val p = e.getVertex(Direction.IN).getProperty[Term]("term")
				if (st.pred != p) {
					val truth = e.getProperty[Truth]("truth")
					val ind = truth.induction(st.truth)
					graph.addStatement(Statement(st.pred, Inherit, p, ind))
					val cmp = truth.comparison(st.truth)
					graph.addStatement(Statement(st.pred, Similar, p, cmp))
				}
			}
		}
	}
	
	// M ~ P, M -> S => P -> S ana'
	def analogy_inv(graph: DNarsGraph, st: Statement): Unit = {
		if (st.copula == Inherit) {
			val m: ScalaVertex = graph.v(st.subj)
			val edges = m.outE(Similar).toList
			for (e <- edges) {
				val p = e.getVertex(Direction.IN).getProperty[Term]("term")
				if (st.pred != p) {
					val truth = e.getProperty[Truth]("truth")
					val ana = truth.analogy(st.truth, true)
					graph.addStatement(Statement(p, Inherit, st.pred, ana))
				}
			}
		}
	}
}