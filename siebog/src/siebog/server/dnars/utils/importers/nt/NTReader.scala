package siebog.server.dnars.utils.importers.nt

import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.io.Source
import com.hp.hpl.jena.rdf.model.Statement
import java.io.StringReader

object NTReader {
	def read(input: String, progress: (Statement, Int) => Boolean): Int = {
		var counter = 0
		var ok = true
		Source
			.fromFile(input)
			.getLines
			.filter { line => line.length > 0 && line.charAt(0) != '#' }
			.takeWhile { _ => ok }
			.foreach { line => {
				val model = ModelFactory.createDefaultModel();
				val reader = model.getReader("N-Triples")
				reader.read(model, new StringReader(line), "")
				val statement = model.listStatements.nextStatement
				
				ok = progress(statement, counter)
				counter += 1
			} }
		counter
	}
}