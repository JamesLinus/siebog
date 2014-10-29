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

package siebog.xjaf.managers.webclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.websocket.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.managers.AgentInitArgs;
import siebog.xjaf.managers.AgentManager;
import siebog.xjaf.radigostlayer.RadigostAgent;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(WebClientManager.class)
@LocalBean
@Path("/webclient")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebClientManagerBean implements WebClientManager {
	private static final long serialVersionUID = 1L;
	private transient Map<String, Session> webClients;

	public WebClientManagerBean() {
		webClients = Collections.synchronizedMap(new HashMap<String, Session>());
	}

	@Override
	public void onWebClientRegistered(String id, Session session) {
		if (webClients.containsKey(id))
			throw new IllegalArgumentException("WebClient ID " + id + " already registered.");
		webClients.put(id, session);
	}

	@Override
	public void onWebClientDeregistered(String id) {
		webClients.remove(id);
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void acceptRadigostAgent(@FormParam("url") String url, @FormParam("aid") String aid,
			@FormParam("state") String state) {
		AgentManager agm = ObjectFactory.getAgentManager();
		AgentClass agClass = new AgentClass(Global.SERVER, RadigostAgent.class.getSimpleName());
		AgentInitArgs args = new AgentInitArgs();
		args.put("url", url);
		args.put("aid", aid);
		args.put("state", state);
		agm.startAgent(agClass, aid + System.currentTimeMillis(), args);
	}
}
