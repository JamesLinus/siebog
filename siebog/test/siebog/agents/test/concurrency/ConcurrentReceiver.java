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

import java.io.Serializable;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class ConcurrentReceiver extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentReceiver.class);

	private static class Buffer implements Serializable {
		private static final long serialVersionUID = 1L;
		private int position;
		private int num;
		private int[] array;

		public Buffer(int size) {
			array = new int[size];
		}

		public void addNext() {
			if (position < array.length) {
				num = num + 1;
				array[position] = num;
				position = position + 1;
			}
		}

		public boolean ok() {
			for (int i = 0; i < array.length - 1; i++)
				if (array[i + 1] - array[i] != 1)
					return false;
			return true;
		}
	}

	private Buffer buff;

	@Override
	protected void onInit(AgentInitArgs args) {
		int buffSize = args.getInt("buffSize", 0);
		buff = new Buffer(buffSize);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST)
			buff.addNext();
		else {
			ACLMessage reply = msg.makeReply(Performative.INFORM);
			reply.content = "" + buff.ok();
			LOG.info("Replying with content: {}", reply.content);
			msm().post(reply);
		}
	}
}
