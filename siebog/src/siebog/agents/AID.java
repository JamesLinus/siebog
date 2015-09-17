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

package siebog.agents;

import java.io.Serializable;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import siebog.radigost.stub.RadigostStub;

/**
 * Agent identifier, consists of the runtime name and the platform identifier, in the form of
 * "name@host".
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public final class AID implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String name;
	private final String host;
	private final String str; // string representation
	private final AgentClass agClass;
	private static final String HOST_NAME = "xjaf"; // TODO Get cluster/host name.
	public static final AID EXTERNAL_CLIENT = new AID("", "", new AgentClass("", ""));

	public AID() {
		name = "";
		host = "";
		str = "";
		agClass = null;
	}

	public AID(String name, AgentClass agClass) {
		this(name, HOST_NAME, agClass);
	}

	public AID(String name, String host, AgentClass agClass) {
		this.name = name;
		this.host = host != null ? host : HOST_NAME;
		this.agClass = agClass;
		str = name + "@" + host;
	}

	public AID(String jsonStr) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			name = json.getString("name");
			host = json.has("host") ? json.getString("host") : HOST_NAME;
			str = name + "@" + host;
			if (json.optBoolean("radigost"))
				agClass = RadigostStub.AGENT_CLASS;
			else
				agClass = new AgentClass(json.getString("agClass"));
		} catch (JSONException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public AID(String name, Class<? extends XjafAgent> siebogAgentClass) {
		this(name, AgentClass.forSiebogEjb(siebogAgentClass));
	}

	@Override
	public int hashCode() {
		return str.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AID other = (AID) obj;
		return str.equals(other.str);
	}

	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", name);
			obj.put("host", host);
			obj.put("agClass", agClass);
			if (agClass.equals(RadigostStub.AGENT_CLASS))
				obj.put("radigost", true); // required by Radigost
			obj.put("str", str);
		} catch (JSONException ex) {
		}
		return obj.toString();
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public AgentClass getAgClass() {
		return agClass;
	}

	public String getStr() {
		return str;
	}
}
