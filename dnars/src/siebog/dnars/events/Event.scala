package siebog.dnars.events

import java.io.Serializable

object EventKind extends Enumeration {
	type EventKind = Value
	val ADDED, UPDATED = Value
}

import EventKind._

class Event(val kind: EventKind, val statement: String) extends Serializable {
	override def toString = kind + " " + statement
}