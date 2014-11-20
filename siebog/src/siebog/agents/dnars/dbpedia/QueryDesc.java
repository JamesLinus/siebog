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

import java.io.Serializable;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class QueryDesc implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String question;
	private final String text;
	private final String allProperties;
	private final String knownProperties;

	public QueryDesc(String question, String text, String allProperties, String knownProperties) {
		this.question = question;
		this.text = text;
		this.allProperties = allProperties;
		this.knownProperties = knownProperties;
	}

	public String getQuestion() {
		return question;
	}

	public String getText() {
		return text;
	}

	public String getAllProperties() {
		return allProperties;
	}

	public String getKnownProperties() {
		return knownProperties;
	}
}
