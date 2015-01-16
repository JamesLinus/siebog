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

package siebog.agents.test.concurrency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.rmi.RemoteException;
import org.junit.Test;
import siebog.agents.test.TestClientBase;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ConcurrencyTest extends TestClientBase {
	private static class TestThread extends Thread {
		private int iterations;
		private AID aid;

		public TestThread(int iterations, AID aid) {
			this.iterations = iterations;
			this.aid = aid;
		}

		@Override
		public void run() {
			for (int i = 0; i < iterations; i++) {
				ObjectFactory.getMessageManager().post(getMessage());
				Thread.yield();
			}
		}

		private ACLMessage getMessage() {
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.receivers.add(aid);
			return msg;
		}
	}

	public ConcurrencyTest() throws RemoteException {
		super("192.168.213.1", "192.168.213.129");
	}

	private static final int BUFF_SIZE = 16384;
	private static final int NUM_THREADS = 6;
	private static final int ITERATIONS = BUFF_SIZE / NUM_THREADS;

	@Test
	public void testConcurrentAccess() {
		AID aid = createAgent();
		Thread[] th = createThreads(aid);
		waitThreads(th);
		ACLMessage result = getResult(aid);
		assertNotNull(result);
		assertTrue(Boolean.parseBoolean(result.content));
	}

	private AID createAgent() {
		AgentClass cls = new AgentClass(Global.SIEBOG_MODULE, ConcurrentReceiver.class.getSimpleName());
		AgentInitArgs args = new AgentInitArgs("buffSize=" + BUFF_SIZE);
		return agm.startAgent(cls, "CR" + System.currentTimeMillis(), args);
	}

	private Thread[] createThreads(AID aid) {
		Thread[] th = new Thread[NUM_THREADS];
		for (int i = 0; i < th.length; i++) {
			th[i] = new TestThread(ITERATIONS, aid);
			th[i].start();
		}
		return th;
	}

	private void waitThreads(Thread[] threads) {
		for (Thread th : threads)
			try {
				th.join();
			} catch (InterruptedException e) {
			}
	}

	private ACLMessage getResult(AID aid) {
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.receivers.add(aid);
		msg.replyTo = testAgentAid;
		msm.post(msg);
		return pollMessage();
	}
}
