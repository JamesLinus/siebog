package siebog.test.xjaf

import org.junit.Before
import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable.ListBuffer
import java.io.File
import siebog.xjaf.fipa.ACLMessage
import org.junit.After
import siebog.SiebogClient

class TestBase(val numNodes: Int, val address: String = "localhost", val testDir: String = "/home/dejan/tmp") {
	val msgQueue = new LinkedBlockingQueue[ACLMessage]
	val agentsModule = "siebog-agents"

	@Before
	def before(): Unit = {
		if (numNodes > 0) {
			val nodes = new ListBuffer[SiebogNode]()
			nodes += new MasterNode(new File(testDir, "siebog0"), address);
			for (i <- 1 to numNodes) {
				val slave = new SlaveNode(new File(testDir, "siebog1"), address, s"slave$i", masterAddr = address, portOffset = i * 100)
				nodes += slave
			}

			XjafTestUtils.start(nodes.toList, fullBuild = true)
		}

		SiebogClient.connect(address)
		XjafTestUtils.startTestAgent(msgQueue)
	}

	@After
	def after(): Unit = {
		if (numNodes > 0) {
			// TODO : terminate servers
		}
	}
}