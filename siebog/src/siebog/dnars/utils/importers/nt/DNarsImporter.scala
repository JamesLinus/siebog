package siebog.dnars.utils.importers.nt

import siebog.dnars.graph.DNarsGraphFactory
import scala.io.Source
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Statement
import siebog.dnars.base.Term
import siebog.dnars.graph.DNarsVertex
import java.util.HashMap
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType
import scala.collection.mutable.Map
import com.tinkerpop.blueprints.Vertex
import siebog.dnars.graph.DNarsGraph
import com.tinkerpop.blueprints.TransactionalGraph
import siebog.dnars.graph.DNarsEdge

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
		//cfg.put("storage.batch-loading", "true")
		val graph = DNarsGraphFactory.create(domain, cfg)
		val bg = BatchGraph.wrap(graph, 1000)
		try {
			graph.eventManager.paused = true
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line =>
					val st = StatementParser(line)
					add(bg, st)
					graph.statements.unpack(st) match {
						case List(st1, st2) =>
							add(bg, st1)
							add(bg, st2)
						case _ =>
					}
					
					counter += 1
					if (counter % 512 == 0)
						println(s"Imported $counter statements...")
				}
			println(s"Done. Total: $counter statements.")
		} finally {
			graph.shutdown()
		}
	}
	
	private def add(graph: TransactionalGraph, st: Statement): Unit = {
		val s = getOrAdd(graph, st.subj)
		val p = getOrAdd(graph, st.pred)
		val e = graph.addEdge(null, s, p, st.copula)
		DNarsEdge(e).truth = st.truth
	}
	
	private def getOrAdd(graph: TransactionalGraph, term: Term): Vertex = {
		var id: Long = map.getOrElse(term.id, -1)
		var v: Vertex = null
		if (id > 0)
			v = graph.getVertex(id)
		else {
			idCounter += 1
			v = graph.addVertex(idCounter)
			map.put(term.id, v.getId().asInstanceOf[Long])
			DNarsVertex(v).term = term
		}
		v
	}
}