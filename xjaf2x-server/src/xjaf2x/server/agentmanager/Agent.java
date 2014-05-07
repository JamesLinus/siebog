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

package xjaf2x.server.agentmanager;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.naming.NamingException;
import xjaf2x.server.Global;
import xjaf2x.server.messagemanager.MessageManagerI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Base class for all agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
@Lock(LockType.READ)
public abstract class Agent implements AgentI
{
	private static final long serialVersionUID = 1L;
	// the access timeout is needed only when the system is under a heavy load.
	// under normal circumstances, all methods return as quickly as possible
	public static final long ACCESS_TIMEOUT = 60000;
	protected final Logger logger = Logger.getLogger(getClass().getName());
	protected AID myAid;
	protected AgentManagerI agm;
	protected MessageManagerI msm;
	private boolean processing;
	private BlockingQueue<ACLMessage> queue;
	private boolean terminated;
	// TODO : replace with the managed executor service of Java EE 7
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	@Resource
	private SessionContext context;

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public final void init(AID aid, Serializable... args) throws NamingException 
	{
		myAid = aid;
		agm = Global.getAgentManager();
		msm = Global.getMessageManager();
		queue = new LinkedBlockingQueue<>();
		onInit(args);
	}

	protected void onInit(Serializable... args)
	{
	}

	/**
	 * Override this method to handle incoming messages.
	 * 
	 * @param msg
	 */
	protected abstract void onMessage(ACLMessage msg);

	protected void onTerminate()
	{
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public final void handleMessage(ACLMessage msg)
	{
		queue.add(msg);
		if (!processing)
			processNextMessage();
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public final void processNextMessage()
	{
		final AgentI me = context.getBusinessObject(AgentI.class);

		if (terminated)
		{
			onTerminate();
			// remove statful beans
			if (getClass().getAnnotation(Stateful.class) != null)
				me.remove();
			return;
		}

		final ACLMessage msg = receiveNoWait();
		if (msg == null)
			processing = false;
		else
		{
			processing = true;
			executor.submit(new Runnable() {
				@Override
				public void run()
				{
					// TODO : check if the access to onMessage is protected
					onMessage(msg);
					me.processNextMessage(); // will acquire lock
				}
			});
		}
	}

	@Override
	@Lock(LockType.WRITE)
	@AccessTimeout(value = ACCESS_TIMEOUT)
	public final void terminate()
	{
		terminated = true;
		if (!processing)
			processNextMessage();
	}

	/**
	 * Retrieves the next message from the queue, or null if the queue is empty.
	 * 
	 * @return ACLMessage object, or null if the queue is empty.
	 */
	protected ACLMessage receiveNoWait()
	{
		return queue.poll();
	}

	/**
	 * Retrieves the next message from the queue, waiting up to the specified
	 * wait time if necessary for the message to become available.
	 * 
	 * @param timeout Maximum wait time, in milliseconds. If zero, the real time
	 *            is not taken into account and the method simply waits until a
	 *            message is available.
	 * @return ACLMessage object, or null if the specified waiting time elapses
	 *         before the message is available.
	 * @throws IllegalArgumentException if timeout < 0.
	 */
	protected ACLMessage receiveWait(long timeout)
	{
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout value cannot be negative.");
		ACLMessage msg = null;
		try
		{
			if (timeout == 0)
				timeout = Long.MAX_VALUE;
			msg = queue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex)
		{
		}
		return msg;
	}

	@Override
	public int hashCode()
	{
		return myAid.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return myAid.equals(((Agent) obj).myAid);
	}

	@Override
	public AID getAid()
	{
		return myAid;
	}

	@Override
	public String getNodeName()
	{
		return System.getProperty("jboss.node.name");
	}

	@Override
	@Remove
	public final void remove()
	{
	}
}
