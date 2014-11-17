package siebog.dnars.utils.importers.nt

import java.io.StringReader

import scala.io.Source

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Statement

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
				try {
					val ntStat = str2nt(line)
					counter += 1
					ok = progress(line, ntStat, counter)
				} catch {
					case ex: Exception =>
						println(s"Error in line $counter: $line")
						ex.printStackTrace
				}
			}
		counter
	}

	def str2nt(str: String): com.hp.hpl.jena.rdf.model.Statement = {
		val model = ModelFactory.createDefaultModel();
		val reader = model.getReader("N-Triples")
		reader.read(model, new StringReader(str), "")
		model.listStatements.nextStatement
	}
}