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

import scala.collection.mutable.ListBuffer

import siebog.server.xjaf.Global
import siebog.server.xjaf.agents.base.AID
import siebog.server.xjaf.agents.fipa.acl.ACLMessage
import siebog.server.xjaf.agents.fipa.acl.Performative
import siebog.server.xjaf.dnarslayer.Event

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class EventDispatcher(val list: ListBuffer[Event], val observers: ListBuffer[AID]) extends Thread {
	override def run: Unit = {
		while (!Thread.interrupted()) {
			try {
				var events = new Array[Event](0)
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
				case e: Throwable =>
					e.printStackTrace()
					return
			}
		}
	}

	private def dispatch(events: Array[Event]): Unit = {
		val acl = new ACLMessage(Performative.INFORM)
		acl.setContent(events)
		observers synchronized {
			observers.foreach { aid => acl.addReceiver(aid) }
		}
		Global.getMessageManager().post(acl)
	}
}