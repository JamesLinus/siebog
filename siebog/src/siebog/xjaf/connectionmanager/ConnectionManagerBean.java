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

package siebog.xjaf.connectionmanager;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import siebog.xjaf.core.XjafAgent;

/**
 * Default connection manager implementation.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Startup
@Remote(ConnectionManager.class)
public class ConnectionManagerBean extends ReceiverAdapter implements MessageListener, ConnectionManager {
	private static final Logger logger = Logger.getLogger(ConnectionManagerBean.class.getName());
	private JChannel channel;

	@PostConstruct
	public void postConstruct() {
		/*
		 * RelayInfo relay = SiebogCluster.getConfig().getRelay(); if (relay == null)
		 * logger.info("Relay not specified, support for remote clusters disabled"); else { try {
		 * channel = new JChannel(SiebogCluster.class.getResource("/site-config.xml"));
		 * channel.connect("xjaf-global-cluster"); channel.setReceiver(new ReceiverAdapter() {
		 * 
		 * }); if (logger.isLoggable(Level.INFO)) logger.info("ConnectionManager initialized"); }
		 * catch (Exception ex) { logger.log(Level.SEVERE, "Unable to create channel", ex); } }
		 */
		
		try {
			channel = new JChannel(getClass().getResourceAsStream("xjaf-tcpA.xml"));
			System.out.println(channel.printProtocolSpec(true));
			channel.setReceiver(this);
			channel.connect("xjaf_master_cluster");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void send(Address dest, XjafAgent agent) throws Exception {
		channel.send(dest, agent);
	}
	
	public List<Address> getMembers() {
		return channel.getView().getMembers();
	}
	
	public Address getLocalAddress() {
		return channel.getAddress();
	}
	
	@Override
	public void receive(Message msg) {
		try {
			System.out.println("received " + msg.getObject());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	};
	
	@Override
	public void viewAccepted(View view) {
		System.out.println(view);
	}

}
