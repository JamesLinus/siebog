package siebog.dnars.utils.importers.nt

import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.io.Source
import java.io.StringReader
import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Statement
import siebog.dnars.base.Truth
import siebog.dnars.base.StatementParser
import com.hp.hpl.jena.rdf.model.RDFNode
import siebog.dnars.base.AtomicTerm
import siebog.dnars.base.Copula._
import siebog.dnars.base.Connector._

object NTReader {
	def read(input: String, progress: (String, Statement, Int) => Boolean): Int = {
		var counter = 0
		var ok = true
		Source
			.fromFile(input)
			.getLines
			.filter { line => line.length > 0 && line.charAt(0) != '#' }
			.takeWhile { _ => ok }
			.foreach { line =>
				{
					val model = ModelFactory.createDefaultModel();
					val reader = model.getReader("N-Triples")
					reader.read(model, new StringReader(line), "")
					val ntStat = model.listStatements.nextStatement
					val statement = toDNarsStatement(ntStat)
					counter += 1
					ok = progress(line, statement, counter)
				}
			}
		counter
	}

	private def toDNarsStatement(ntStat: com.hp.hpl.jena.rdf.model.Statement): Statement = {
		// subject-predicate-object becomes
		// (x subject object) -> predicate
		val termSubj = getAtomicTerm(ntStat.getSubject)
		val termPred = getAtomicTerm(ntStat.getPredicate)
		val termObjt = getAtomicTerm(ntStat.getObject)

		val subj = CompoundTerm(Product, List(termSubj, termObjt))
		val statement = Statement(subj, Inherit, termPred, Truth(1.0, 0.9))

		// make sure everything's ok
		var str = statement.toString
		StatementParser(str)
	}

	private def getAtomicTerm(node: RDFNode): AtomicTerm = {
		val str = node.toString
			.trim
			.replaceAll("""\s""", "_")
			.replace("(", "{")
			.replace(")", "}")
		AtomicTerm(str)
	}
}