package siebog.test.xjaf

import siebog.core.Siebog
import scala.sys.process._
import java.io.File
import org.junit.Assert._
import siebog.xjaf.core.AgentClass
import siebog.core.Global
import siebog.xjaf.managers.AgentInitArgs
import siebog.utils.ObjectFactory

object XjafTestUtils {
	val baseDir = "../siebog-core"
	val zipFile = s"$baseDir/dist/siebog.zip"
	val address = "localhost"

	def start(numNodes: Int, testDir: File, fullBuild: Boolean): Unit = {
		if (fullBuild)
			buildSiebog(numNodes, testDir)

		startCluster(numNodes, testDir)

		val agClass = new AgentClass(Global.SERVER, "TestAgent")
		val args = new AgentInitArgs(s"remoteHost->$address")
		ObjectFactory.getAgentManager().startAgent(agClass, "testAgent", args)
	}

	private def startCluster(numNodes: Int, testDir: File): Unit = {
		for (i <- 0 until numNodes) {
			val nodeDir = new File(testDir, s"siebog$i/siebog")
			val isMaster = i == 0
			val node =
				if (isMaster)
					new MasterNode(nodeDir, address)
				else
					new SlaveNode(nodeDir, address, s"slave$i", address, 100 * i)
			startNode(node)
			if (isMaster)
				Thread.sleep(8000)
		}
	}

	private def buildSiebog(numNodes: Int, testDir: File): Unit = {
		val baseDir = new File("../siebog-core")
		val antCmd = Seq("ant", "dist")
		val exitCode: Int = Process(antCmd, Some(baseDir)).!<
		assertEquals(0, exitCode)
		for (i <- 0 until numNodes) {
			val dir = new File(testDir, s"siebog$i")
			unzipTo(dir)
		}
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
		val obj = new Object
		new Thread() {
			override def run(): Unit = {
				val p = Process(cmd, Some(node.dir), "JBOSS_HOME" -> node.jbossHome)
				p.lines_!.foreach { s =>
					println(s)
					if (s.toLowerCase.contains("siebog node ready"))
						obj synchronized { obj.notify }
				}
			}
		}.start
		obj synchronized { obj.wait }
	}

	def main(args: Array[String]) {
		start(1, new File("/home/dejan/tmp"), false)
	}
}