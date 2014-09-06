package siebog.dnars.utils.importers.nt

import siebog.dnars.graph.DNarsGraphFactory
import scala.io.Source
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Statement
import siebog.dnars.base.Term
import siebog.dnars.graph.DNarsVertex
import java.util.HashMap
import scala.collection.mutable.Map
import com.tinkerpop.blueprints.Vertex
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsEdge
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io.StringReader
import com.hp.hpl.jena.rdf.model.RDFNode
import siebog.dnars.base.AtomicTerm
import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Connector._
import siebog.dnars.base.Copula._
import siebog.dnars.base.Truth
import java.io.PrintWriter

object DNarsImporter {
	val map = Map[String, Long]()
	var idCounter = 0L

	def apply(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile DomainName")
			return
		}
		val input = args(0)
		val domain = args(1)

		println(s"Importing from $input...")
		val cfg = new HashMap[String, Any]
		val graph = DNarsGraphFactory.create(domain, cfg)
		graph.eventManager.paused = true
		try {
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line =>
					val statement = StatementParser(line)
					add(graph, statement)
					graph.statements.unpack(statement) match {
						case List(st1, st2) =>
							add(graph, st1)
							add(graph, st2)
						case _ =>
					}

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

	private def add(graph: DNarsGraph, st: Statement): Unit = {
		val s = graph.getOrAddV(st.subj)
		val p = graph.getOrAddV(st.pred)
		val e = graph.addEdge(null, s, p, st.copula)
		DNarsEdge(e).truth = st.truth
	}

	private def saveLastLine(line: String): Unit = {
		val out = new PrintWriter("last_line");
		try {
			out.println(line)
		} finally {
			out.close
		}
	}

}