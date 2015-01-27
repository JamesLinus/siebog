package siebog.dnars.utils.importers.nt

import scala.io.Source

import com.tinkerpop.blueprints.Vertex

import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Term
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraph.unwrap
import siebog.dnars.graph.DNarsGraphFactory
import siebog.dnars.graph.Wrappers.edge2DNarsEdge
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex

object DNarsImporter {
	val vertices = new java.util.HashMap[String, Vertex]()
	
	def initEmptyGraph(domain: String): Unit = {
		val props = new java.util.HashMap[String, Any]()
		props.put("init-schema", "true")
		DNarsGraphFactory.create(domain, props).shutdown()
	}
	
	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile DomainName")
			return
		}
		val input = args(0)
		val domain = args(1)
		
		initEmptyGraph(domain)
		
		println(s"Importing from $input...")
		val props = new java.util.HashMap[String, Any]()
		props.put("storage.batch-loading", true)
		val graph = DNarsGraphFactory.create(domain, props)
		graph.paused = true
		try {
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line =>
					val statement = StatementParser(line)
					val time2 = System.currentTimeMillis()
					add(graph, statement)

					counter += 1
					if (counter % 10000 == 0) {
						print("\r                                              ")
						print(s"\rImported $counter statements...")
					}
				}
			println(s"Done. Total: ${counter * 3} statements, ${vertices.size()} vertices.")
		} catch {
			case ex: Throwable =>
				ex.printStackTrace
		} finally {
			graph.shutdown
			System.exit(0)
		}
	}

	def add(graph: DNarsGraph, st: Statement): Unit = {
		addSt(graph, st)
		st.unpack match {
			case List(st1, st2) =>
				addSt(graph, st1)
				addSt(graph, st2)
			case _ =>
		}
	}

	private def addSt(graph: DNarsGraph, st: Statement): Unit = {
		val s = getVertex(graph, st.subj) // graph.getOrAddV(st.subj)
		val p = getVertex(graph, st.pred) // graph.getOrAddV(st.pred)
		val e = graph.addEdge(null, s, p, st.copula)
		e.truth = st.truth
	}
	
	private def getVertex(graph: DNarsGraph, t: Term): Vertex = {
		var v = vertices.get(t.id)
		if (v == null) {
			v = graph.addV(null)
			v.term = t
			vertices.put(t.id, v)
		}
		v
	}
}
