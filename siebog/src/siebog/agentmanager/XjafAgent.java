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

package siebog.agentmanager;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.connectionmanager.ConnectionManager;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.MessageManager;
import siebog.utils.ObjectFactory;

/**
 * Base class for all agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
@Lock(LockType.READ)
public abstract class XjafAgent implements Agent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(XjafAgent.class);
	// the access timeout is needed only when the system is under a heavy load.
	// under normal circumstances, all methods should return as quickly as possible
	public static final long ACCESS_TIMEOUT = 5;
	protected AID myAid;
	private AgentManager agm;
	private MessageManager msm;
	@EJB private ConnectionManager cm;

	// TODO : Restore support for heartbeats.
	// private transient long hbHandle;

	@Override
	public void init(AID aid, AgentInitArgs args) {
		myAid = aid;
		onInit(args);
	}

	protected void onInit(AgentInitArgs args) {
	}

	@Override
	public void handleMessage(ACLMessage msg) {
		// TODO : check if the access to onMessage is protected
		// TODO : Restore support for heartbeats.
		if (msg instanceof HeartbeatMessage) {
			boolean repeat = onHeartbeat(msg.content);
			if (repeat)
				; // executor().signalHeartbeat(hbHandle);
			else
				; // executor().cancelHeartbeat(hbHandle);
		} else {
			if (filter(msg)) {
				try {
					onMessage(msg);
				} catch (Exception ex) {
					LOG.warn("Error while delivering message {}.", msg, ex);
				}
			}
		}
	}

	protected abstract void onMessage(ACLMessage msg);

	protected boolean onHeartbeat(String content) {
		return false;
	}

	protected void onTerminate() {
	}

	@Override
	@Remove
	public void stop() {
		try {
			onTerminate();
		} catch (Exception ex) {
			LOG.warn("Error in onTerminate.", ex);
		}
	}

	protected ACLMessage receiveNoWait() {
		return null; // queue.poll(); // TODO : Implement receiveNoWait.
	}

	protected ACLMessage receiveWait(long timeout) {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout value cannot be negative.");
		ACLMessage msg = null;
		// TODO : Implement receiveWait.
		// try {
		// if (timeout == 0)
		// timeout = Long.MAX_VALUE;
		// msg = queue.poll(timeout, TimeUnit.MILLISECONDS);
		// } catch (InterruptedException ex) {
		// }
		return msg;
	}

	@Override
	public int hashCode() {
		return myAid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return myAid.equals(((XjafAgent) obj).myAid);
	}

	/**
	 * Before being finally delivered to the agent, the message will be passed to this filtering
	 * function.
	 * 
	 * @param msg
	 * @return If false, the message will be discarded.
	 */
	protected boolean filter(ACLMessage msg) {
		return true;
	}

	protected void registerHeartbeat(String content) {
		// TODO : Restore support for heartbeats.
		// hbHandle = executor().registerHeartbeat(myAid, 500, content);
	}

	protected void registerHeartbeat() {
		registerHeartbeat("");
	}

	public AID getAid() {
		return myAid;
	}

	protected String getNodeName() {
		return System.getProperty("jboss.node.name");
	}

	@Override
	public String ping() {
		return getNodeName();
	}

	protected AgentManager agm() {
		if (agm == null)
			agm = ObjectFactory.getAgentManager();
		return agm;
	}

	protected MessageManager msm() {
		if (msm == null)
			msm = ObjectFactory.getMessageManager();
		return msm;
	}
	
	protected ConnectionManager cm() {
		return cm;
	}
}