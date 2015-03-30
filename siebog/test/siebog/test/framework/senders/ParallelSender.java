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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import siebog.interaction.ACLMessage;
import siebog.interaction.MessageManager;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ParallelSender implements MsgSender {
	private ConcurrentLinkedDeque<ACLMessage> messages;
	private int numThreads = 1;

	public ParallelSender() {
		messages = new ConcurrentLinkedDeque<>();
	}

	@Override
	public void setMessages(Collection<ACLMessage> messages) {
		this.messages = new ConcurrentLinkedDeque<>(messages);
	}

	@Override
	public void send() {
		Thread[] threads = createThreads();
		startThreads(threads);
		waitThreads(threads);
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	private Thread[] createThreads() {
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = createThread();
		}
		return threads;
	}

	private void startThreads(Thread[] threads) {
		for (Thread th : threads) {
			th.start();
		}
	}

	private void waitThreads(Thread[] threads) {
		try {
			for (Thread th : threads) {
				th.join();
			}
		} catch (InterruptedException ex) {
		}
	}

	private Thread createThread() {
		return new Thread() {
			@Override
			public void run() {
				MessageManager mngr = ObjectFactory.getMessageManager();
				ACLMessage msg = messages.poll();
				while (msg != null && !Thread.interrupted()) {
					msg.replyWith = MessageManager.REPLY_WITH_TEST;
					mngr.post(msg);
					msg = messages.poll();
				}
			}
		};
	}
}
