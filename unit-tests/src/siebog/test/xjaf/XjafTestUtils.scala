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
	var testAgentAid: AID = null

	/**
	 * The function will create n sub-dirs in 'testDir' (named 'siebog0', 'siebog1', etc.),
	 * each representing a separate server.
	 *
	 * If fullBuild, the fuction will first invoke the Ant build script that builds
	 * the deployment archive.
	 */
	def start(nodes: Seq[TestNode], fullBuild: Boolean, msgQueue: BlockingQueue[ACLMessage]): Unit = {
		val reg = LocateRegistry.createRegistry(1099)
		reg.rebind("TestAgentListener", new TestAgentListenerImpl(msgQueue))

		if (fullBuild)
			buildSiebog(nodes)

		nodes.foreach { node =>
			startNode(node)
			Thread.sleep(1000)
		}

		SiebogCluster.init
		val agClass = new AgentClass(Global.SERVER, "TestAgent")
		val args = new AgentInitArgs(s"remoteHost->localhost")
		testAgentAid = ObjectFactory.getAgentManager().startAgent(agClass, "testAgent", args)
	}

	private def buildSiebog(nodes: Seq[TestNode]): Unit = {
		val baseDir = new File("../siebog-core")
		val antCmd = Seq("ant", "dist")
		val exitCode: Int = Process(antCmd, Some(baseDir)).!<
		assertEquals(0, exitCode)

		val srcDir = new File(baseDir, "dist/siebog")
		nodes.foreach { node =>
			if (node.dir.exists)
				FileUtils.deleteDirectory(node.dir)
			val created = node.dir.mkdirs
			assertTrue("Cannot create " + node.dir, created)
			FileUtils.copyDirectory(srcDir, node.dir)
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
	}
}

class TestAgentListenerImpl(val queue: BlockingQueue[ACLMessage]) extends UnicastRemoteObject with TestAgentListener {
	override def onMessage(msg: ACLMessage): Unit =
		queue.add(msg)
}