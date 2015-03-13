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

package siebog.agents.dnars.dbpedia;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.InvocationCallback;
import org.hornetq.utils.json.JSONArray;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import siebog.agents.AID;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AnnotationProcessor implements InvocationCallback<String> {
	private static final Logger logger = Logger.getLogger(AnnotationProcessor.class.getName());
	private AID aid;

	public AnnotationProcessor(AID aid) {
		this.aid = aid;
	}

	@Override
	public void completed(String response) {
		HashSet<String> uris = getURIs(response);
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.receivers.add(aid);
		msg.contentObj = uris;
		ObjectFactory.getMessageManager().post(msg);
	}

	private HashSet<String> getURIs(String response) {
		HashSet<String> uris = new HashSet<>(); // there can be duplicates in the response
		try {
			JSONObject obj = new JSONObject(response);
			JSONArray resources = obj.getJSONArray("Resources");
			for (int i = 0, n = resources.length(); i < n; i++)
				uris.add(resources.getJSONObject(i).getString("@URI"));
		} catch (JSONException ex) {
			logger.log(Level.WARNING, "Error while parsing DBpedia Spotlight response:\n" + response, ex);
		}
		return uris;
	}

	@Override
	public void failed(Throwable ex) {
		logger.log(Level.WARNING, "Error while annotating text.", ex);
	}

}
