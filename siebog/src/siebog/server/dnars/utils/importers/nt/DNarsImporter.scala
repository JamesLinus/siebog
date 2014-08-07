package siebog.server.dnars.utils.importers.nt

import siebog.server.dnars.graph.DNarsGraphFactory
import scala.io.Source
import siebog.server.dnars.base.StatementParser
import java.util.HashMap

object DNarsImporter {
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
		try {
			graph.eventManager.paused = true
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line =>
					val st = StatementParser(line)
					graph.statements.add(st)

					counter += 1
					if (counter % 512 == 0)
						println(s"Imported $counter statements...")
				}
			println(s"Done. Total: $counter statements.")
		} finally {
			graph.shutdown()
		}
	}
}