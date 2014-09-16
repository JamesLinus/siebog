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

package siebog.agents.dnars.resspeedtest;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import scala.Option;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;
import siebog.dnars.base.Term;
import siebog.dnars.inference.Resolution;
import siebog.xjaf.core.Agent;
import siebog.xjaf.dnarslayer.DNarsAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.AgentInitArgs;
import com.tinkerpop.blueprints.Vertex;

/**
 * 
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class ResolutionSpeedTest extends DNarsAgent {
	private static final long serialVersionUID = 1L;
	private int numIterations;
	private SpeedResults speedResults;

	@Override
	protected void onInit(AgentInitArgs args) {
		super.onInit(args);
		graph.removeObserver(myAid.toString());
		numIterations = Integer.parseInt(args.get("numIterations"));

		String host = args.get("SpeedResultsHost");
		try {
			Registry reg = LocateRegistry.getRegistry(host, 1099);
			speedResults = (SpeedResults) reg.lookup("SpeedResults");
		} catch (RemoteException | NotBoundException ex) {
			throw new IllegalStateException("Cannot connect to the SpeedResults service.", ex);
		}
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			Vertex v = graph.getRandomVertex();
			String term = (String) v.getProperty("term");
			String str;
			if (Math.random() < 0.5)
				str = "? -> " + term;
			else
				str = term + " -> ?";
			Statement question = StatementParser.apply(str);

			long start = System.currentTimeMillis();
			Term answer = Resolution.answer(graph, question).getOrElse(null);
			long total = System.currentTimeMillis() - start;

			logger.info(str + " ::: " + (answer != null ? answer : "N/A"));
			try {
				speedResults.add(myAid, total);

				if (--numIterations > 0)
					msm.post(msg);
			} catch (RemoteException ex) {
				logger.log(Level.WARNING, "Error while storing speed results.", ex);
			}
		}
	}
}
