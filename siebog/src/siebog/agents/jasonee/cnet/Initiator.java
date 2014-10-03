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

package siebog.agents.jasonee.cnet;

import java.io.File;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import siebog.SiebogClient;
import siebog.agents.xjaf.RemoteAgentListener;
import siebog.core.Global;
import siebog.jasonee.JasonEEProject;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.AgentInitArgs;
import siebog.xjaf.managers.MessageManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Initiator extends UnicastRemoteObject implements RemoteAgentListener {
	private static final long serialVersionUID = 1L;
	private long startTime;
	private AtomicInteger numPart;
	private MessageManager msm;
	private AID remoteAgentId;
	private String cfpContent;
	private String acceptedContent;

	public Initiator(String rmiHost, int numPart, int primeLimit, String[] nodes) throws RemoteException,
			URISyntaxException {
		this.numPart = new AtomicInteger(numPart);
		cfpContent = String.format("cfp(%d)", primeLimit);
		acceptedContent = String.format("acceptProposal(%d)", primeLimit);

		File f = new File(getClass().getResource("cnet.mas2j").toURI());
		JasonEEProject p = JasonEEProject.loadFromFile(f);

		String[] slaves;
		if (nodes.length == 1)
			slaves = new String[0];
		else
			slaves = Arrays.copyOfRange(nodes, 1, nodes.length);
		SiebogClient.connect(nodes[0], slaves);
		ObjectFactory.getJasonEEStarter().start(p);

		AgentClass remoteAgent = new AgentClass(Global.SERVER, "RemoteAgent");
		AgentInitArgs args = new AgentInitArgs();
		args.put("remoteHost", rmiHost);
		remoteAgentId = ObjectFactory.getAgentManager().startAgent(remoteAgent, "RemoteAgent", args);

		msm = ObjectFactory.getMessageManager();
	}

	public void start() {
		startTime = System.currentTimeMillis();
		final int n = numPart.get();
		for (int i = 1; i <= n; i++) {
			AID aid = new AID("c" + i);
			ACLMessage cfp = new ACLMessage(Performative.INFORM);
			cfp.sender = remoteAgentId;
			cfp.receivers.add(aid);
			cfp.content = cfpContent;
			msm.post(cfp);
		}
		System.out.println("CFPs sent.");
	}

	@Override
	public void onMessage(ACLMessage msg) throws RemoteException {
		if (msg.content.startsWith("propose")) {
			ACLMessage reply = msg.makeReply(Performative.INFORM);
			reply.sender = remoteAgentId;
			reply.content = acceptedContent;
			msm.post(reply);
		} else if (numPart.decrementAndGet() == 0) {
			long total = System.currentTimeMillis() - startTime;
			System.out.println("Total time: " + total + " ms");
			System.exit(0);
		}
	}

	public static void main(String[] args) throws URISyntaxException, RemoteException {
		final String prop = "java.rmi.server.hostname";

		if (args.length != 3) {
			System.out.println("I need three arguments: NumOfParticipants PrimeLimit CommaListOfNodes.");
			System.out.println("In addition, set property " + prop + " to the address of this computer.");
			return;
		}

		int numPart = Integer.parseInt(args[0]);
		int primeLimit = Integer.parseInt(args[1]);
		String[] nodes = args[2].split(",");
		if (nodes.length == 0) {
			System.out.println("Cluster node(s) required.");
			return;
		}

		String rmiHost = System.getProperty(prop);
		if (rmiHost == null || rmiHost.isEmpty()) {
			System.out.println("Property " + prop + " not defined, using localhost.");
			rmiHost = "localhost";
		}
		Initiator i = new Initiator(rmiHost, numPart, primeLimit, nodes);
		Registry reg = LocateRegistry.createRegistry(1099);
		reg.rebind(RemoteAgentListener.class.getSimpleName(), i);
		i.start();
	}
}
