package dnars.utils.importers.nt

import com.hp.hpl.jena.rdf.model.ModelFactory
import dnars.graph.DNarsGraphFactory
import scala.io.Source
import java.io.StringReader
import dnars.base.AtomicTerm
import dnars.base.CompoundTerm
import dnars.base.Statement
import dnars.base.Copula.Inherit
import dnars.base.Connector.Product
import dnars.base.Truth
import scala.collection.mutable.ListBuffer
import dnars.events.Event
import com.hp.hpl.jena.rdf.model.RDFNode

object NTImporter {
	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile TargetKeyspace")
			return
		}
		val fileName = args(0)
		val keyspace = args(1)
		
		val graph = DNarsGraphFactory.create(keyspace)
		try {		
			val model = ModelFactory.createDefaultModel();
			val reader = model.getReader("N-Triples")
			var counter = 0
			val events = ListBuffer[Event]()
			println(s"Reading from $fileName into $keyspace")
			Source
				.fromFile(fileName)
				.getLines
				.filter { line => line.length > 0 && line.charAt(0) != '#' }
				.foreach { line =>
					reader.read(model, new StringReader(line), "")
					val ntStat = model.listStatements.nextStatement
					
					// subject-predicate-object becomes
					// (x subject object) -> predicate
					val termSubj = getAtomicTerm(ntStat.getSubject)
					val termPred = getAtomicTerm(ntStat.getPredicate)
					val termObjt = getAtomicTerm(ntStat.getObject)
					
					val subj = CompoundTerm(Product, List(termSubj, termObjt))
					val statement = Statement(subj, Inherit, termPred, Truth(1.0, 0.9))
					graph.statements.add(statement, events)
					
					counter += 1
					if (counter % 300 == 0) 
						println(s"Read $counter statements...")
				}
			println(s"Done. Total: $counter statements.")
		} finally {
			graph.shutdown()
		}
	}
	
	private def getAtomicTerm(node: RDFNode): AtomicTerm = {
		val str = node.toString
			.trim
			.replace("""\s""", "_")
		AtomicTerm(str)
	}
}