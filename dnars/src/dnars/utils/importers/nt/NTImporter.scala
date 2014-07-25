package dnars.utils.importers.nt

import java.io.BufferedReader
import java.io.FileReader
import scala.io.Source

object NTImporter extends App {
	val fileName = "/home/dejan/tmp/persondata_en.nt"
	Source
		.fromFile(fileName)
		.getLines
		.foreach { line => {
			val st = NTLineParser(line)
			println(st)
		}}
}