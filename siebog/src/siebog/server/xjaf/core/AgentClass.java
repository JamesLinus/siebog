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

package siebog.server.xjaf.core;

import java.io.Serializable;

/**
 * Description of a deployed agent.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AgentClass implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final char SEPARATOR = '$';
	private final String module;
	private final String ejbName;

	public AgentClass(String module, String ejbName) {
		this.module = module;
		this.ejbName = ejbName;
	}

	public String getModule() {
		return module;
	}

	public String getEjbName() {
		return ejbName;
	}

	@Override
	public String toString() {
		return module + SEPARATOR + ejbName;
	}

	public static AgentClass valueOf(String str) {
		int n = str.lastIndexOf(SEPARATOR);
		return new AgentClass(str.substring(0, n), str.substring(n + 1));
	}
}
