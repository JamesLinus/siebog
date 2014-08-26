/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package siebog.server.dnars.events

import java.util.logging.Level
import java.util.logging.Logger
import scala.collection.mutable.ListBuffer
import siebog.server.xjaf.core.AID
import siebog.server.xjaf.dnarslayer.Event
import siebog.server.xjaf.fipa.ACLMessage
import siebog.server.xjaf.fipa.Performative
import siebog.server.xjaf.managers.ManagerFactory
import siebog.server.SiebogCluster

/**
 * Implementation of the Event Dispatch Thread.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class EDT(val list: ListBuffer[Event], val observers: ListBuffer[AID]) extends Thread {
	val logger = Logger.getLogger(classOf[EDT].getName)

	override def run: Unit = {
		while (!Thread.interrupted()) {
			try {
				var events: Array[Event] = null
				list synchronized {
					while (list.length == 0)
						list.wait
					events = list.toArray
					list.clear
				}
				dispatch(events)
			} catch {
				case _: InterruptedException =>
					return
				case ex: Exception =>
					logger.log(Level.WARNING, "Exception in EDT.", ex)
			}
		}
	}

	private def dispatch(events: Array[Event]): Unit = {
		val acl = new ACLMessage(Performative.INFORM)
		// TODO : Event[] to String
		//acl.setContent(events)
		observers synchronized {
			observers.foreach { aid => acl.addReceiver(aid) }
		}
		ManagerFactory.getMessageManager().post(acl)
	}
}