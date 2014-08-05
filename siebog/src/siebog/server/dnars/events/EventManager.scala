package siebog.server.dnars.events

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

import scala.collection.mutable.ListBuffer

class EventManager {
	private val list = new ListBuffer[Event]
	private val observers = new ListBuffer[EventObserver]
	private var _paused: Boolean = false
	
	val NUM_DISPATCHERS = 1
	for (i <- 0 until NUM_DISPATCHERS)
		new EventDispatcher(list, observers).start
	
	def addEvent(event: Event): Unit = 
		list synchronized { 
			list += event
			if (!paused)
				list.notify
		}
	
	def addObserver(eo: EventObserver): Unit = 
		observers synchronized { observers += eo }
	
	def remObserver(eo: EventObserver): Unit = 
		observers synchronized { observers -= eo}
	
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