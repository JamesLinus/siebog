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

package dnars.siebog.agents.dbpedia;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.ws.rs.client.InvocationCallback;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AnnotationProcessor implements InvocationCallback<String> {
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationProcessor.class);
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
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response);
			Iterator<Entry<String, JsonNode>> i = root.path("Resources").getFields();
			while (i.hasNext()) {
				JsonNode node = i.next().getValue();
				uris.add(node.path("@URI").asText());
			}
		} catch (IOException ex) {
			LOG.error("Cannot process response from DBpedia Spotlight.", ex);
		}
		return uris;
	}

	@Override
	public void failed(Throwable ex) {
		LOG.warn("Error while annotating text.", ex);
	}

}
