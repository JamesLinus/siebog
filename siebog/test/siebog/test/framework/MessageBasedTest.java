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

package siebog.test.framework;

import siebog.test.framework.receivers.MsgReceiverBuilder;
import siebog.test.framework.senders.MsgSenderBuilder;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MessageBasedTest {
	private MsgSenderBuilder senderBuilder;
	private MsgReceiverBuilder receiverBuilder;

	private MessageBasedTest() {
	}

	public static MessageBasedTest which() {
		return new MessageBasedTest();
	}

	public MsgSenderBuilder sends() {
		if (senderBuilder != null) {
			throw new IllegalStateException("Message sender already set.");
		}
		senderBuilder = new MsgSenderBuilder(this);
		return senderBuilder;
	}

	public MsgReceiverBuilder receives() {
		if (receiverBuilder != null) {
			throw new IllegalStateException("Message receiver already set.");
		}
		receiverBuilder = new MsgReceiverBuilder(this);
		return receiverBuilder;
	}

	public void execute() {
		send();
		receive();
	}

	private void send() {
		if (senderBuilder != null) {
			senderBuilder.getSender().send();
		}
	}

	private void receive() {
		if (receiverBuilder != null) {
			receiverBuilder.getReceiver().receive();
		}
	}
}
