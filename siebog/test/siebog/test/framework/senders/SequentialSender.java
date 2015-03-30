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

package siebog.test.framework.senders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import siebog.interaction.ACLMessage;
import siebog.interaction.MessageManager;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SequentialSender implements MsgSender {
	private List<ACLMessage> messages = new ArrayList<>();

	@Override
	public void setMessages(Collection<ACLMessage> messages) {
		this.messages = new ArrayList<>(messages);
	}

	@Override
	public void send() {
		MessageManager mngr = ObjectFactory.getMessageManager();
		for (ACLMessage msg : messages) {
			msg.replyWith = MessageManager.REPLY_WITH_TEST;
			mngr.post(msg);
		}
	}
}
