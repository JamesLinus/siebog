package siebog.test.xjaf

import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import siebog.utils.ObjectFactory
import siebog.xjaf.core.AgentClass
import siebog.xjaf.fipa.ACLMessage
import siebog.xjaf.fipa.Performative
import org.junit.After

class XjafAgentsTest {
	val msgQueue = new LinkedBlockingQueue[ACLMessage]
	val agentsModule = "siebog-agents"

	@Before
	def before(): Unit = {
		val testDir = "/home/dejan/tmp"
		val address = "localhost"

		val nodes = List(
			new MasterNode(new File(testDir, "siebog0"), address),
			new SlaveNode(new File(testDir, "siebog1"), address, "slave1", masterAddr = address, portOffset = 100))

		XjafTestUtils.start(nodes, fullBuild = true, msgQueue)
	}

	@Test
	def testPingPong(): Unit = {
		val agm = ObjectFactory.getAgentManager

		val pingAid = agm.startAgent(new AgentClass(agentsModule, "Ping"), "Ping", null)

		val pongName = "Pong"
		agm.startAgent(new AgentClass(agentsModule, "Pong"), pongName, null)

		val msm = ObjectFactory.getMessageManager
		val message = new ACLMessage(Performative.REQUEST)
		message.receivers.add(pingAid)
		message.content = pongName
		message.replyTo = XjafTestUtils.testAgentAid
		msm.post(message)

		val reply = msgQueue.poll(1, TimeUnit.SECONDS)

		assertNotNull(reply)
		assertEquals("Invalid performative: " + reply.performative, Performative.INFORM, reply.performative)
	}

	@After
	def after(): Unit = {
		// TODO : terminate servers
	}
}