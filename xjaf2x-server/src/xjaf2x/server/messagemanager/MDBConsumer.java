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
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import xjaf2x.server.Global;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/test"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MDBConsumer implements MessageListener
{
	private static final Logger logger = Logger.getLogger(MDBConsumer.class.getName());
	private MessageManagerI msm;
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{
			msm = Global.getMessageManager();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MDBConsumer initialization error.", ex);
		}
	}
	
	@Override
	public void onMessage(Message msg)
	{
		try
		{
			ACLMessage acl = (ACLMessage) ((ObjectMessage) msg).getObject();
			msm.deliver(acl);
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while delivering a message.", ex);
		}
	}

}
