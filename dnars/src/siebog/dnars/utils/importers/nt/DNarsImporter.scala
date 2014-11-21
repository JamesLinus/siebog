package siebog.dnars.utils.importers.nt

import scala.io.Source

import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsEdge
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraph.unwrap
import siebog.dnars.graph.DNarsGraphFactory

object DNarsImporter {
	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile DomainName")
			return
		}
		val input = args(0)
		val domain = args(1)

		println(s"Importing from $input...")
		val graph = DNarsGraphFactory.create(domain)
		graph.eventManager.paused = true
		try {
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line =>
					val statement = StatementParser(line)
					add(graph, statement)

					counter += 1
					if (counter % 16384 == 0) {
						print("\r                                              ")
						print(s"\rImported $counter statements...")
					}
				}
			println(s"Done. Total: ${counter * 3} statements.")
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
		val s = graph.getOrAddV(st.subj)
		val p = graph.getOrAddV(st.pred)
		val e = graph.addEdge(null, s, p, st.copula)
		DNarsEdge(e).truth = st.truth
	}
}
