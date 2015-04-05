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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import siebog.interaction.ACLMessage;
import siebog.interaction.ACLMsgBuilder;
import siebog.test.framework.MessageBasedTest;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MsgSenderBuilder {
	private MessageBasedTest testBuilder;
	private MsgSender sender;

	public MsgSenderBuilder(MessageBasedTest testBuilder) {
		this.testBuilder = testBuilder;
	}

	public MsgSenderBuilder inSequence() {
		return setSender(new SequentialSender());
	}

	public MsgSenderBuilder inParallel() {
		return setSender(new ParallelSender());
	}

	public MsgSenderBuilder withThreadCount(int threadCount) {
		if (sender == null || !(sender instanceof ParallelSender)) {
			throw new IllegalStateException("Parallel sender required.");
		}
		((ParallelSender) sender).setNumThreads(threadCount);
		return this;
	}

	public MsgSenderBuilder messages(ACLMessage... messages) {
		return setMessages(Arrays.asList(messages));
	}

	public MsgSenderBuilder messages(ACLMsgBuilder... msgBuilders) {
		List<ACLMessage> messages = new ArrayList<>();
		for (ACLMsgBuilder builder : msgBuilders) {
			messages.add(builder.build());
		}
		return setMessages(messages);
	}

	public MessageBasedTest and() {
		return testBuilder;
	}

	public MessageBasedTest build() {
		return testBuilder;
	}

	public MsgSender getSender() {
		if (sender == null) {
			throw new IllegalStateException("Message sender not set.");
		}
		return sender;
	}

	private MsgSenderBuilder setSender(MsgSender sender) {
		if (sender != null) {
			throw new IllegalStateException("Message sender already set.");
		}
		this.sender = sender;
		return this;
	}

	private MsgSenderBuilder setMessages(Collection<ACLMessage> messages) {
		if (sender == null) {
			throw new IllegalStateException("Message sender not set.");
		}
		sender.setMessages(messages);
		return this;
	}
}
