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

package siebog.xjaf.messagemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * Default message manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */
@Stateless
@Remote(MessageManager.class)
@LocalBean
@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageManagerBean implements MessageManager {
	private static final Logger logger = Logger.getLogger(MessageManagerBean.class.getName());
	private Connection connection;
	private Topic topic;
	private Session session;
	private MessageProducer producer;

	public static ConnectionFactory getConnectionFactory() {
		return ObjectFactory.lookup("java:jboss/exported/jms/RemoteConnectionFactory",
				ConnectionFactory.class);
	}

	public static Topic getTopic() {
		return ObjectFactory.lookup("java:jboss/exported/jms/topic/siebog", Topic.class);
	}

	@PostConstruct
	public void postConstruct() {
		try {
			ConnectionFactory connectionFactory = getConnectionFactory();
			connection = connectionFactory.createConnection();
			topic = getTopic();
			session = connection.createSession();
			producer = session.createProducer(topic);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Unable to initialize the JMS.", ex);
		}
	}

	@PreDestroy
	public void preDestroy() {
		try {
			session.close();
		} catch (JMSException e) {
			//e.printStackTrace();
		}
		try {
			connection.close();
		} catch (JMSException e) {
			//e.printStackTrace();
		}
	}

	@GET
	@Path("/")
	public List<String> getPerformatives() {
		final Performative[] arr = Performative.values();
		List<String> list = new ArrayList<>(arr.length);
		for (Performative p : arr)
			list.add(p.toString());
		return list;
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	// NOTE: Using @Asynchronous causes an exception
	// https://issues.jboss.org/browse/WFLY-2515
	public void post(ACLMessage msg) {
		// TODO : Check if the agent/subscriber exists
		// http://hornetq.sourceforge.net/docs/hornetq-2.0.0.BETA5/user-manual/en/html/management.html#d0e5742
		for (AID aid : msg.receivers) {
			if (aid == null)
				throw new IllegalArgumentException("AID cannot be null.");
			try {
				ObjectMessage jmsMsg = session.createObjectMessage(msg);
				jmsMsg.setStringProperty("aid", aid.getStr());
				producer.send(jmsMsg);
			} catch (Exception ex) {
				logger.warning(ex.getMessage());
			}
		}
	}

	@Override
	public String ping() {
		return "Pong from " + Global.getNodeName();
	}

}
