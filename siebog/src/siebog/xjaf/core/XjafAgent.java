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

package siebog.xjaf.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import siebog.utils.ObjectFactory;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.agentmanager.AgentManager;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.messagemanager.MessageManager;
import siebog.xjaf.messagemanager.MessageManagerBean;

/**
 * Base class for all agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
@Lock(LockType.READ)
public abstract class XjafAgent implements Agent, MessageListener {
	private static final long serialVersionUID = 1L;
	private static ConnectionFactory connectionFactory;
	private static Connection connection;
	private static Topic topic;
	private transient Session session;
	// the access timeout is needed only when the system is under a heavy load.
	// under normal circumstances, all methods should return as quickly as possible
	public static final long ACCESS_TIMEOUT = 5;
	protected final Logger logger = Logger.getLogger(getClass().getName());
	protected AID myAid;
	private transient AgentManager agm;
	private transient MessageManager msm;
	// TODO : Restore support for heartbeats.
	// private transient long hbHandle;

	static {
		try {
			connectionFactory = MessageManagerBean.getConnectionFactory();
			topic = MessageManagerBean.getTopic();
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (Exception ex) {
			Logger.getLogger(XjafAgent.class.getName()).log(Level.SEVERE,
					"Unable to initialize the JMS.", ex);
		}
	}

	@Override
	public void init(AID aid, AgentInitArgs args) {
		myAid = aid;
		onInit(args);
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String selector = "aid = '" + myAid.getStr() + "'";
			MessageConsumer consumer = session.createConsumer(topic, selector);
			consumer.setMessageListener(this);
		} catch (JMSException ex) {
			logger.log(Level.SEVERE, "Unable to connect to the JMS topic.", ex);
		}
	}

	protected void onInit(AgentInitArgs args) {
	}

	/**
	 * Override this method to handle incoming messages.
	 * 
	 * @param msg
	 */
	protected abstract void onMessage(ACLMessage msg);

	@Override
	public void onMessage(Message jmsMsg) {
		try {
			ACLMessage msg = (ACLMessage) ((ObjectMessage) jmsMsg).getObject();
			// TODO : check if the access to onMessage is protected
			// TODO : Restore support for heartbeats.
			if (msg instanceof HeartbeatMessage) {
				boolean repeat = onHeartbeat(msg.content);
				if (repeat)
					; // executor().signalHeartbeat(hbHandle);
				else
					; // executor().cancelHeartbeat(hbHandle);
			} else {
				if (filter(msg))
					try {
						onMessage(msg);
					} catch (Exception ex) {
						logger.log(Level.WARNING, "Error while delivering message " + msg, ex);
					}
			}
		} catch (JMSException ex) {
			logger.log(Level.WARNING, "Unable to extract the ACL message.", ex);
		}
	}

	protected boolean onHeartbeat(String content) {
		return false;
	}

	protected void onTerminate() {
	}

	@Override
	@Remove
	public void stop() {
		// invokeOnTerminate();
		// closeSession();
		// TODO : Implement XjafAgent.stop()
	}

	private void invokeOnTerminate() {
		try {
			onTerminate();
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Error in onTerminate.", ex);
		}
	}

	private void closeSession() {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException ex) {
				logger.log(Level.WARNING, "Error while closing the JMS session.", ex);
			} finally {
				session = null;
			}
		}
	}

	/**
	 * Retrieves the next message from the queue, or null if the queue is empty.
	 * 
	 * @return ACLMessage object, or null if the queue is empty.
	 */
	protected ACLMessage receiveNoWait() {
		return null; // queue.poll(); // TODO : Implement receiveNoWait.
	}

	/**
	 * Retrieves the next message from the queue, waiting up to the specified wait time if necessary
	 * for the message to become available.
	 * 
	 * @param timeout Maximum wait time, in milliseconds. If zero, the real time is not taken into
	 *            account and the method simply waits until a message is available.
	 * @return ACLMessage object, or null if the specified waiting time elapses before the message
	 *         is available.
	 * @throws IllegalArgumentException if timeout &lt; 0.
	 */
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
	public void ping() {
		logger.info(myAid + " pinged.");
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
}
