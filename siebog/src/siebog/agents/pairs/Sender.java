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

package siebog.agents.pairs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;

/**
 * Sends a request to the Receiver agent and calculates the message round-trip time (RTT). A number
 * of iterations can be performed, averaging calculated RTTs in order reduce any noise in the
 * results. In any case, the end RTT is reported to a RMI service.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Sender extends XjafAgent {
	private static final long serialVersionUID = -5648061637952026195L;
	private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
	private int numIterations;
	private int iterationIndex;
	private AID receiver;
	private String content;
	private String resultsServiceAddr;
	private long totalTime;

	@Override
	protected void onInit(AgentInitArgs args) {
		AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, Receiver.class.getSimpleName());
		receiver = new AID(args.get("rcvrAid", null), agClass);
		numIterations = args.getInt("numIterations", 0);
		// create message content
		int contentLength = args.getInt("contentLength", 0);
		content = makeContent(contentLength);
		resultsServiceAddr = args.get("resultsServiceAddr", null).toString();
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			iterationIndex = 0;
			totalTime = 0;
			postMsg();
		} else {
			++iterationIndex;
			totalTime += System.currentTimeMillis() - Long.parseLong(msg.inReplyTo);
			if (iterationIndex < numIterations)
				postMsg();
			else {
				long avg = totalTime / numIterations;
				try {
					Registry reg = LocateRegistry.getRegistry(resultsServiceAddr);
					ResultsService results = (ResultsService) reg.lookup("ResultsService");
					results.add(avg, myAid.getHost());
				} catch (RemoteException | NotBoundException ex) {
					LOG.error("Cannot connect to ResultsService.", ex);
				} finally {
					agm().stopAgent(myAid);
				}
			}
		}
	}

	private void postMsg() {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = myAid;
		msg.receivers.add(receiver);
		msg.content = content;
		msg.replyWith = System.currentTimeMillis() + "";
		msm().post(msg);
	}

	private String makeContent(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append("A");
		return sb.toString();
	}
}
