package siebog.test.xjaf

import siebog.core.Siebog
import scala.sys.process._
import java.io.File
import org.junit.Assert._
import siebog.xjaf.core.AgentClass
import siebog.core.Global
import siebog.xjaf.managers.AgentInitArgs
import siebog.utils.ObjectFactory
import org.apache.commons.io.FileUtils
import scala.collection.mutable.ListBuffer
import siebog.SiebogCluster
import java.rmi.server.UnicastRemoteObject
import siebog.xjaf.test.TestAgentListener
import java.util.concurrent.BlockingQueue
import siebog.xjaf.fipa.ACLMessage
import java.rmi.registry.LocateRegistry
import siebog.xjaf.core.AID

object XjafTestUtils {
	val baseDir = new File("../siebog-core")
	val address = "localhost"
	var testAgentAid: AID = null

	/**
	 * The function will create n sub-dirs in 'testDir' (named 'siebog0', 'siebog1', etc.),
	 * each representing a separate server.
	 *
	 * If fullBuild, the fuction will first invoke the Ant build script that builds
	 * the deployment archive.
	 */
	def start(numNodes: Int, testDir: File, fullBuild: Boolean, msgQueue: BlockingQueue[ACLMessage]): Unit = {
		val reg = LocateRegistry.createRegistry(1099)
		reg.rebind("TestAgentListener", new TestAgentListenerImpl(msgQueue))

		val nodeDirs =
			for (i <- 0 until numNodes)
				yield new File(testDir, s"siebog$i")

		if (fullBuild)
			buildSiebog(nodeDirs)

		startCluster(nodeDirs)

		SiebogCluster.init
		val agClass = new AgentClass(Global.SERVER, "TestAgent")
		val args = new AgentInitArgs(s"remoteHost->$address")
		testAgentAid = ObjectFactory.getAgentManager().startAgent(agClass, "testAgent", args)
	}

	private def buildSiebog(nodeDirs: Seq[File]): Unit = {
		val antCmd = Seq("ant", "dist")
		val exitCode: Int = Process(antCmd, Some(baseDir)).!<
		assertEquals(0, exitCode)

		val srcDir = new File(baseDir, "dist/siebog")
		nodeDirs.foreach { dstDir =>
			if (dstDir.exists)
				FileUtils.deleteDirectory(dstDir)
			val created = dstDir.mkdirs
			assertTrue("Cannot create " + dstDir, created)
			FileUtils.copyDirectory(srcDir, dstDir)
		}
	}

	private def startCluster(nodeDirs: Seq[File]): Unit = {
		var i = 0
		nodeDirs.foreach { nodeDir =>
			val isMaster = i == 0
			val node =
				if (isMaster)
					new MasterNode(nodeDir, address)
				else
					new SlaveNode(nodeDir, address, s"slave$i", address, 100 * i)
			startNode(node)
			i += 1
		}
	}

	private def startNode(node: TestNode): Unit = {
		val starter = new File(node.dir, "siebog-start.jar").getCanonicalPath
		val cmd = List("java", "-jar", starter) ::: node.getArgs
		val obj = new Object
		var startedOk = true
		new Thread() {
			override def run(): Unit = {
				val p = Process(cmd, Some(node.dir), "JBOSS_HOME" -> node.jbossHome)
				p.lines_!.foreach { s =>
					println(s)
					if (s.contains("""Deployed "siebog-agents.war""""))
						obj synchronized {
							startedOk = true
							obj.notify
						}
					else if (s.contains("""WildFly 1.0.0.Alpha3 "Kenny" stopped"""))
						obj synchronized {
							startedOk = false
							obj.notify
						}
				}
			}
		}.start
		obj synchronized {
			obj.wait
			assertTrue("Node failed to start.", startedOk)
		}
		Thread.sleep(1000)
	}
}

class TestAgentListenerImpl(val queue: BlockingQueue[ACLMessage]) extends UnicastRemoteObject with TestAgentListener {
	override def onMessage(msg: ACLMessage): Unit =
		queue.add(msg)
}