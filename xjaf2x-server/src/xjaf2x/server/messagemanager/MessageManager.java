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

package xjaf2x.server.messagemanager;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import org.infinispan.Cache;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.Global;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Default message manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(MessageManagerI.class)
@Clustered
@Lock(LockType.READ)
public class MessageManager implements MessageManagerI
{
	private static final Logger logger = Logger.getLogger(MessageManager.class.getName());
	private Cache<AID, AgentI> runningAgents;
	private static ConnectionFactory factory;
	private static Queue queue;
	private Connection conn;
	private Session session;
	private MessageProducer producer;
	
	static
	{
		try
		{
			final Context ctx = Global.getContext();
			factory = (ConnectionFactory) ctx.lookup("java:/JmsXA");
			queue = (Queue) ctx.lookup("queue/test");
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MessageManager initialization error.");
		}
	}
	
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			runningAgents = Global.getRunningAgents();
			conn = factory.createConnection();
			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(queue);
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MessageManager initialization error.", ex);
		}
	}
	
	@PreDestroy
	public void preDestroy()
	{
		try
		{
			producer.close();
		} catch (JMSException e)
		{
		}
		try
		{
			session.close();
		} catch (JMSException e)
		{
		}
		try
		{
			conn.close();
		} catch (JMSException e)
		{
		}
	}

	@Override
	public void post(final ACLMessage message)
	{
		/*try
		{
			producer.send(session.createObjectMessage(message));
		} catch (JMSException ex)
		{
			logger.log(Level.SEVERE, "", ex);
		}*/
		deliver(message);
	}
	
	@Override
	public void deliver(ACLMessage msg)
	{
		for (AID aid : msg.getReceivers())
		{
			if (aid == null)
				continue;
			AgentI agent = runningAgents.get(aid);
			if (agent != null)
				agent.handleMessage(msg);
			else
				logger.info("Agent not running: [" + aid + "]");
		}
	}
}
