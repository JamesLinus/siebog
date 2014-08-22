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

package siebog.server.xjaf.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.fipa.ACLMessage;
import siebog.server.xjaf.managers.AgentManager;
import siebog.server.xjaf.managers.MessageManager;

/**
 * Base class for all agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
@Lock(LockType.READ)
public abstract class AgentBase implements Agent {
	private static final long serialVersionUID = 1L;
	// the access timeout is needed only when the system is under a heavy load.
	// under normal circumstances, all methods should return as quickly as possible
	public static final long ACCESS_TIMEOUT = 60000;
	protected final Logger logger = Logger.getLogger(getClass().getName());
	private Agent myself;
	protected AID myAid;
	protected AgentManager agm;
	protected MessageManager msm;
	private boolean processing;
	private BlockingQueue<ACLMessage> queue;
	private boolean terminated;
	// TODO : replace with the managed executor service of Java EE 7
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	@Resource(name = "sessionContext")
	private SessionContext context;

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public void init(AID aid, Map<String, String> args) {
		myAid = aid;
		agm = Global.getAgentManager();
		msm = Global.getMessageManager();
		queue = new LinkedBlockingQueue<>();
		// a reference to myself
		try {
			InitialContext ctx = new InitialContext();
			SessionContext ejbCtx = (SessionContext) ctx.lookup("java:comp/EJBContext");
			myself = ejbCtx.getBusinessObject(Agent.class);
		} catch (NamingException ex) {
			logger.log(Level.WARNING, "Unable to obtain a reference to the business object.", ex);
		}
		onInit(args);
	}

	protected void onInit(Map<String, String> args) {
	}

	/**
	 * Override this method to handle incoming messages.
	 * 
	 * @param msg
	 */
	protected abstract void onMessage(ACLMessage msg);

	protected void onTerminate() {
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public void handleMessage(ACLMessage msg) {
		queue.add(msg);
		if (!processing)
			processNextMessage();
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public void processNextMessage() {
		if (terminated) {
			onTerminate();
			// remove statful beans
			if (getClass().getAnnotation(Stateful.class) != null)
				myself.remove();
			return;
		}

		final ACLMessage msg = receiveNoWait();
		if (msg == null)
			processing = false;
		else {
			processing = true;
			executor.submit(new Runnable() {
				@Override
				public void run() {
					// TODO : check if the access to onMessage is protected
					if (filter(msg))
						onMessage(msg);
					myself.processNextMessage(); // will acquire lock
				}
			});
		}
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public void stop() {
		terminated = true;
		if (!processing)
			processNextMessage();
	}

	/**
	 * Retrieves the next message from the queue, or null if the queue is empty.
	 * 
	 * @return ACLMessage object, or null if the queue is empty.
	 */
	protected ACLMessage receiveNoWait() {
		return queue.poll();
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
		try {
			if (timeout == 0)
				timeout = Long.MAX_VALUE;
			msg = queue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
		}
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
		return myAid.equals(((AgentBase) obj).myAid);
	}

	@Override
	@Remove
	public void remove() {
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
}
