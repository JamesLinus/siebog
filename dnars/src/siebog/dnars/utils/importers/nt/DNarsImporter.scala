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
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph
import com.tinkerpop.blueprints.TransactionalGraph

object DNarsImporter {
	def initGraph(domain: String): Unit = 
		DNarsGraphFactory.create(domain).shutdown()

	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile DomainName")
			return
		}
		val input = args(0)
		val domain = args(1)
		
		// cannot build schema in batch loading
		initGraph(domain)

		println(s"Importing from $input...")
		val props = new java.util.HashMap[String, Any]()
		props.put("storage.batch-loading", true)
		val graph = BatchGraph.wrap(DNarsGraphFactory.create(domain, props))
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
			println(s"Done. Total: ${counter * 3} statements.")
		} catch {
			case ex: Throwable =>
				ex.printStackTrace
		} finally {
			graph.shutdown()
			System.exit(0)
		}
	}

	def add(graph: TransactionalGraph, st: Statement): Unit = {
		addSt(graph, st)
		st.unpack match {
			case List(st1, st2) =>
				addSt(graph, st1)
				addSt(graph, st2)
			case _ =>
		}
	}

	private def addSt(graph: TransactionalGraph, st: Statement): Unit = {
		val s = getVertex(graph, st.subj) // graph.getOrAddV(st.subj)
		val p = getVertex(graph, st.pred) // graph.getOrAddV(st.pred)
		val e = graph.addEdge(null, s, p, st.copula)
		e.truth = st.truth
	}

	private def getVertex(graph: TransactionalGraph, t: Term): Vertex = {
		var v = graph.getVertex(t.id)
		if (v == null) {
			v = graph.addVertex(t.id)
			v.term = t
		}
		v
	}
}
