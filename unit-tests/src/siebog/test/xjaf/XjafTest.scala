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
import siebog.SiebogClient

class XjafTest extends TestBase(0, "localhost") {

	@Test
	def testPingPong(): Unit = {
		runPingPong
		val reply = msgQueue.poll(10, TimeUnit.SECONDS)
		assertNotNull(reply)
		assertEquals(Performative.INFORM, reply.performative)
	}

	def runPingPong(): Unit = {
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
	}
}

object XjafTest {
	def main(args: Array[String]) {
		val test = new XjafTest
		SiebogClient.connect(test.address)
		new XjafTest().runPingPong
	}
}