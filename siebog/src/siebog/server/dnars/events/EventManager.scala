package siebog.server.dnars.events

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable.ListBuffer
import siebog.server.xjaf.base.AID
import siebog.server.xjaf.dnarslayer.Event

class EventManager {
	private val list = new ListBuffer[Event]
	private val observers = new ListBuffer[AID]()
	private var _paused: Boolean = false
	
	val NUM_DISPATCHERS = 1
	for (i <- 0 until NUM_DISPATCHERS)
		new EDT(list, observers).start
	
	def addEvent(event: Event): Unit = 
		list synchronized { 
			list += event
			if (!paused)
				list.notify
		}
	
	def addObserver(aid: AID): Unit = 
		observers synchronized { observers += aid }
	
	def remObserver(aid: AID): Unit = 
		observers synchronized { observers -= aid }
	
	def paused = this synchronized { _paused }
	
	def paused_=(value: Boolean): Unit = this synchronized {
		_paused = value
		if (!value)
			list synchronized {
				if (list.size > 0)
					list.notify
			}
	}
}