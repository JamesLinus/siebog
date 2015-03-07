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

package siebog.jasonee.environment;

import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.xjaf.core.AID;

/**
 * Outcome of a scheduled action.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ActionFeedbackMessage extends ACLMessage {
	private static final long serialVersionUID = 1L;
	private boolean success;
	private String userData;

	public ActionFeedbackMessage(AID aid, boolean success, String userData) {
		super(Performative.REQUEST);
		receivers.add(aid);
		this.success = success;
		this.userData = userData;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getUserData() {
		return userData;
	}
}
