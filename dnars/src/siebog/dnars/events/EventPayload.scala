package siebog.dnars.events

import java.io.Serializable

class EventPayload(val kind: EventKind, val statement: String) extends Serializable {
	override def toString = kind + " " + statement
}