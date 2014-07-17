package dnars.events

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

import scala.collection.mutable.ListBuffer

object EventManager {
	private val list = new ListBuffer[Event]
	private val observers = new ListBuffer[EventObserver]
	
	val NUM_DISPATCHERS = 1
	for (i <- 0 until NUM_DISPATCHERS)
		new EventDispatcher(list, observers).start
	
	def addEvents(events: ListBuffer[Event]): Unit = 
		list synchronized { 
			list ++= events
			list.notify
		}
	
	def addObserver(eo: EventObserver): Unit = 
		observers synchronized { observers += eo }
	
	def remObserver(eo: EventObserver): Unit = 
		observers synchronized { observers -= eo}
}