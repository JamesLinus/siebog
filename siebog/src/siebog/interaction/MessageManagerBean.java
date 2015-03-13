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

package siebog.interaction;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.core.Global;

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
	private static final Logger LOG = LoggerFactory.getLogger(MessageManagerBean.class);
	@Inject
	private JMSFactory factory;
	private Session session;
	private MessageProducer producer;

	@PostConstruct
	public void postConstruct() {
		session = factory.getSession();
		producer = factory.getProducer(session);
	}

	@PreDestroy
	public void preDestroy() {
		try {
			session.close();
		} catch (JMSException e) {
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
		for (int i = 0; i < msg.receivers.size(); i++) {
			AID aid = msg.receivers.get(i);
			if (aid == null)
				throw new IllegalArgumentException("AID cannot be null.");
			try {
				ObjectMessage jmsMsg = session.createObjectMessage(msg);
				// TODO See meessage grouping in a cluster
				// http://docs.jboss.org/hornetq/2.2.5.Final/user-manual/en/html/message-grouping.html
				jmsMsg.setStringProperty("JMSXGroupID", aid.getStr());
				jmsMsg.setIntProperty("AIDIndex", i);
				producer.send(jmsMsg);
			} catch (Exception ex) {
				LOG.warn(ex.getMessage());
			}
		}
	}

	@Override
	public String ping() {
		return "Pong from " + Global.getNodeName();
	}
}
