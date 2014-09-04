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

		println(s"Reading from $input...")
		val cfg = new HashMap[String, Any]
		var graph = DNarsGraphFactory.create(domain, cfg)
		graph.eventManager.paused = true
		try {
			val total = NTReader.read(input, (line, statement, counter) => {
				add(graph, statement)
				graph.statements.unpack(statement) match {
					case List(st1, st2) =>
						add(graph, st1)
						add(graph, st2)
					case _ =>
				}
				if (counter % 1024 == 0)
					println(s"Imported $counter statements...")
				if (counter % 1048576 == 0) {
					graph.shutdown
					graph = DNarsGraphFactory.create(domain, cfg)
					saveLastLine(line)
				}
				true
			})
			println(s"Done. Total: ${total * 3} statements.")
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