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

package siebog.test.framework.receivers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import siebog.test.framework.MessageBasedTest;
import siebog.test.framework.receivers.MsgReceiver.ReceiveType;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MsgReceiverBuilder {
	private MessageBasedTest testBuilder;
	private MsgReceiver receiver;

	public MsgReceiverBuilder(MessageBasedTest testBuilder) {
		this.testBuilder = testBuilder;
		receiver = new BlockingReceiver();
	}

	public MsgReceiverBuilder within(long timeout, TimeUnit timeUnit) {
		receiver.setTimeout(timeout, timeUnit);
		return this;
	}

	public MsgReceiverBuilder messages(MsgPattern... patterns) {
		receiver.setMessagePatterns(Arrays.asList(patterns));
		return this;
	}

	public MsgReceiverBuilder messages(MsgPatternBuilder... patterns) {
		MsgPattern[] msgPat = new MsgPattern[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			msgPat[i] = patterns[i].build();
		}
		return messages(msgPat);
	}

	public MsgReceiverBuilder anyOf() {
		receiver.setReceiveType(ReceiveType.ANY);
		return this;
	}

	public MsgReceiverBuilder allInNoOrder() {
		receiver.setReceiveType(ReceiveType.ALL_NO_ORDER);
		return this;
	}

	public MsgReceiverBuilder allInExactOrder() {
		receiver.setReceiveType(ReceiveType.ALL_IN_ORDER);
		return this;
	}

	public MessageBasedTest and() {
		return done();
	}

	public MessageBasedTest build() {
		return done();
	}

	public MsgReceiver getReceiver() {
		if (receiver == null) {
			throw new IllegalStateException("Message receiver not set.");
		}
		return receiver;
	}

	private MessageBasedTest done() {
		if (receiver.getReceiveType() == null) {
			throw new IllegalStateException("Missing receive type (e.g. any, all-in-order, etc.).");
		}
		if (receiver.getMessagePatterns() == null) {
			throw new IllegalStateException("Missing receive message patterns.");
		}
		return testBuilder;
	}
}
