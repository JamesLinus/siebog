package siebog.test.xjaf

import java.io.File

abstract class TestNode(val dir: File, val isMaster: Boolean, val address: String) {
	val jbossHome: String = new File(dir, "wildfly-9.x").getCanonicalPath

	def getArgs: List[String] = {
		val mode = if (isMaster) "master" else "slave"
		List(s"--mode=$mode", s"--address=$address")
	}
}

class MasterNode(override val dir: File, override val address: String) extends TestNode(dir, true, address) {

}

class SlaveNode(override val dir: File, override val address: String, val name: String,
	val masterAddr: String, val portOffset: Int) extends TestNode(dir, false, address) {

	override def getArgs: List[String] =
		super.getArgs ::: List(s"--name=$name", s"--master=$masterAddr", s"--port-offset=$portOffset")
}