package siebog.test.xjaf

import siebog.core.Siebog
import scala.sys.process._
import java.io.File
import org.junit.Assert._

object XjafTestUtils {
	val baseDir = "../siebog-core"
	val zipFile = s"$baseDir/dist/siebog.zip"
	val address = "localhost"

	def startCluster(numNodes: Int, testDir: File): Unit = {
		buildSiebog
		for (i <- 0 to numNodes) {
			val dir = new File(testDir, s"siebog$i")
			unzipTo(dir)

			val nodeDir = new File(dir, "siebog")
			val node =
				if (i == 0)
					new MasterNode(nodeDir, address)
				else
					new SlaveNode(nodeDir, address, s"slave$i", address, 100 * i)
			startNode(node)
		}
	}

	private def buildSiebog: Unit = {
		val baseDir = new File("../siebog-core")
		val antCmd = Seq("ant", "dist")
		val exitCode: Int = Process(antCmd, Some(baseDir)).!<
		assertEquals(0, exitCode)
	}

	private def unzipTo(dir: File): Unit = {
		val rmCmd = Seq("rm", "-fr", dir.getCanonicalPath)
		val rmExitCode = Process(rmCmd).!<
		assertEquals(0, rmExitCode)

		val unzipCmd = Seq("unzip", zipFile, "-d", dir.getCanonicalPath)
		val exitCode = Process(unzipCmd).!<
		assertEquals(0, exitCode)
	}

	private def startNode(node: TestNode): Unit = {
		val starter = new File(node.dir, "siebog-start.jar").getCanonicalPath
		val cmd = List("java", "-jar", starter) ::: node.getArgs
		Process(cmd, Some(node.dir), "JBOSS_HOME" -> node.jbossHome).run
	}

	def main(args: Array[String]) {
		startCluster(2, new File("/home/dejan/tmp"))
	}
}