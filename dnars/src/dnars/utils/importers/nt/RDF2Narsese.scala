package dnars.utils.importers.nt

import java.io.PrintWriter
import com.hp.hpl.jena.rdf.model.RDFNode

object RDF2Narsese {
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
			val total = NTReader.read(input, (st, counter) => {
				out.println("<(*," +
					getStr(st.getSubject) + "," +
					getStr(st.getObject) + ") --> " +
					getStr(st.getPredicate) + 
					">. %1.0;0.9%")
				val completed = counter + 1
				if (completed % 4096 == 0) 
					println(s"Converted $completed statements...")
				true
			})
			
			println(s"Done. Total: $total statements.")
		} finally {
			out.close()
		}
	}
	
	private def getStr(node: RDFNode): String = 
		node.toString
			.trim
			.replaceAll("""\s""", "_")
			.replace("%", "$")
}