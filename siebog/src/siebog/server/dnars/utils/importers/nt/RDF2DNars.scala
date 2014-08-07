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
	def apply(args: Array[String]): Unit = {
		if (args.length != 3) {
			println("I need 3 arguments: InputFile OutputFile LinesPerFile")
			return
		}
		val input = args(0)
		val output = args(1)
		val linesPerFile = args(2).toInt
		
		println(s"Reading from $input...")
		var out: PrintWriter = null 
		var linesInFile = 0
		var fileNum = 0
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
				
				if (out == null) {
					fileNum += 1
					val fileName = output + "." + fileNum
					println(s"Writing to file $fileName")
					out = new PrintWriter(fileName)
					linesInFile = 0
				}
				
				out.println(str)
				
				linesInFile += 1
				if (linesInFile == linesPerFile) {
					out.close()
					out = null
				}
				
				if (counter % 4096 == 0) 
					println(s"Processed $counter statements...")
				true
			})
			println(s"Done. Total: $total statements.")
		} finally {
			if (out != null)
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