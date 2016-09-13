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

package siebog.connectionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentManager;
import siebog.utils.FileUtils;

/**
 * Default connection manager implementation.
 * 
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
@Singleton
@Startup
@Path("/connection")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectionManager {
	private List<String> connections = new ArrayList<String>();
	private String hostIp;
	
	@EJB private AgentManager agm;
	
	@PostConstruct
	public void init() {
		System.out.println("############################ CONNECTION MANAGER Started ######################");
		String masterIp = null;
		
		try {
			File f = FileUtils.getFile(ConnectionManager.class, "", "connections.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			masterIp = properties.getProperty("masterIp");
			this.hostIp = properties.getProperty("myIp");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(masterIp != null && !"".equals(masterIp)) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://"+masterIp+"/Siebog/connection");
			ConnectionManagerRestAPI rest = rtarget.proxy(ConnectionManagerRestAPI.class);
			connections = rest.newConnection(this.hostIp);
			connections.remove(this.hostIp);
			connections.add(masterIp);
		}
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> newConnection(String connection) {
		for(String c : connections) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://"+c+"/Siebog/connection/new");
			ConnectionManagerRestAPI rest = rtarget.proxy(ConnectionManagerRestAPI.class);
			rest.addConnection(connection);
		}
		connections.add(connection);
		return connections;
	}
	
	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addConnection(String connection) {
		connections.add(connection);
	}
	
	@POST
	@Path("/move")
	@Consumes(MediaType.APPLICATION_JSON)
	public void moveAgent(Agent agent) {
		agm.reconstructAgent(agent);
	}
}
