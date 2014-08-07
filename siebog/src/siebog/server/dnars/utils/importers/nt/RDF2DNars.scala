package siebog.server.dnars.utils.importers.nt

import java.io.PrintWriter
import com.hp.hpl.jena.rdf.model.RDFNode
import siebog.server.dnars.base.AtomicTerm
import siebog.server.dnars.base.CompoundTerm
import siebog.server.dnars.base.Connector.Product
import siebog.server.dnars.base.Copula.Inherit
import siebog.server.dnars.base.Statement
import siebog.server.dnars.base.Truth
import siebog.server.dnars.base.StatementParser

object RDF2DNars {
	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile OutputFile")
			return
		}
		val input = args(0)
		val output = args(1)
		
		println(s"Reading from $input...")
		val out = new PrintWriter(output)
		try {
			val total = NTReader.read(input, (ntStat, counter) => {
				// subject-predicate-object becomes
				// (x subject object) -> predicate
				val termSubj = getAtomicTerm(ntStat.getSubject)
				val termPred = getAtomicTerm(ntStat.getPredicate)
				val termObjt = getAtomicTerm(ntStat.getObject)
				
				val subj = CompoundTerm(Product, List(termSubj, termObjt))
				val statement = Statement(subj, Inherit, termPred, Truth(1.0, 0.9))
				
				var str = statement.toString
				StatementParser(str) // make sure everything's ok
				out.println(str)
				
				val completed = counter + 1
				if (completed % 4096 == 0) 
					println(s"Processed $completed statements...")
				true
			})
			println(s"Done. Total: $total statements.")
		} finally {
			out.close()
		}
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