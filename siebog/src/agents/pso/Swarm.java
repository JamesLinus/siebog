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

package agents.pso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.Agent;
import siebog.server.xjaf.agents.base.AgentI;
import siebog.server.xjaf.agents.fipa.acl.ACLMessage;
import siebog.server.xjaf.agents.fipa.acl.Performative;

/**
 * 
 * Implementation of an PSO starter agent. It initializes all the Particles, and keeps track of the results.
 * 
 * @author <a href="mailto:simic.dragan@hotmail.com">Dragan Simic</a>
 */
@Stateful
@Remote(AgentI.class)
public class Swarm extends Agent {

	private static final long serialVersionUID = -7864076456995372014L;
	private static final Logger logger = Logger.getLogger(Swarm.class.getName());

	// TODO - Do we need to use 'args' to load values dynamically? Using
	// hard-coded values for testing
	int numberParticles = 10;
	int numberIterations = 1000;
	int dimension = 2;
	// range for X values
	double minX = -100.0;
	double maxX = 100.0;

	// iteration counter
	int iteration = 0;
	int iteratedParticles = 0;

	// best results
	double[] bestGlobalPosition = new double[dimension];
	double bestGlobalFitness = Double.MAX_VALUE;

	/**
	 * 
	 * @see xjaf2x.server.agm.Agent#onInit(java.io.Serializable[])
	 */
	@Override
	protected void onInit(Map<String, Serializable> args) {

		logger.info("PsoStarter agent running.");

		logger.fine("Begin Particle Swarm Optimization demonstration");
		logger.fine("Objective function to minimize has dimension = " + dimension);

		// TODO - load arguments? (numberParticles?, numberIterations?, minX?,
		// maxX?, dimension? )

		logger.fine("Range for all X values is " + minX + " <= x <= " + maxX);
		logger.fine("Number of iterations = " + numberIterations);
		logger.fine("Number of particles in swarm = " + numberParticles);

		logger.fine("Initializing swarm with random positions/solutions.");
		AID particleAid;
		for (int i = 0; i < numberParticles; ++i) {
			particleAid = new AID(Global.SERVER, "Particle", "Particle" + i);
			Map<String, Serializable> mapArgs = new HashMap<>();

			mapArgs.put("dimension", dimension);
			mapArgs.put("minx", minX);
			mapArgs.put("maxx", maxX);
			agm.start(particleAid, mapArgs);
		}

		logger.info("Entering main PSO processing loop");
		iterate();

	}

	/**
	 * One iteration of the whole swarm. Finishes execution if numberIterations is reached.
	 */
	private void iterate() {
		if (iteration < numberIterations) {
			iteration++;
			iteratedParticles = 0;

			// request iteration from all particles
			for (int i = 0; i < numberParticles; ++i) {

				// find particle
				AID particleAidPattern = new AID(null, "Particle", "Particle" + i);
				AID particleAID = agm.getRunning(particleAidPattern).get(0);

				// compose message
				ACLMessage message = new ACLMessage();
				message.setPerformative(Performative.REQUEST);

				PsoMessage psoMessage = new PsoMessage(PsoMessage.ITERATE_PARTICLE, bestGlobalFitness,
						bestGlobalPosition);
				message.setContent(psoMessage);
				message.setSender(myAid);
				message.addReceiver(particleAID);
				msm.post(message);

			}
		} else {
			finish();
		}

	}

	/**
	 * Handles incoming messages.
	 * 
	 * @see xjaf2x.server.agm.Agent#onMessage(xjaf2x.server.msm.fipa.acl.ACLMessage)
	 */
	@Override
	protected void onMessage(ACLMessage message) {

		PsoMessage psoMessage = (PsoMessage) message.getContent();

		if (message.getPerformative() == Performative.REQUEST) {

			if (psoMessage.getAction().equals(PsoMessage.UPDATE_GLOBAL_SOLUTION)) {
				if (psoMessage.getFitness() < bestGlobalFitness) {
					// logger.info("Updated best result. AID: " + message.getSender() + " , fitness: "
					// + psoMessage.getFitness());
					bestGlobalFitness = psoMessage.getFitness();
					bestGlobalPosition = psoMessage.getPosition();
				}
			}

		} else if (message.getPerformative() == Performative.INFORM) {
			// count responses from articles, so that we know when to proceed to next iteration
			iteratedParticles++;
			if (iteratedParticles == numberParticles) {
				iterate();
			}
		}
	}

	/**
	 * Prints results and terminates all agents (particles and swarm)
	 */
	private void finish() {

		// print results
		logger.info("Processing complete");
		logger.info("Final best fitness = " + bestGlobalFitness);
		logger.info("Best position/solution: ");
		for (int i = 0; i < bestGlobalPosition.length; ++i) {
			logger.info("x" + i + " = " + String.format("%.16f", bestGlobalPosition[i]));
		}

		// stop all particles
		for (int i = 0; i < numberParticles; ++i) {

			// find particle
			AID particleAidPattern = new AID(null, "Particle", "Particle" + i);
			AID particleAID = agm.getRunning(particleAidPattern).get(0);

			// Looks like 'stop' is not yet implemented, so it is not working yet
			// All particles should be stopped/terminated at this point
			agm.stop(particleAID);
		}

		// do we need to terminate swarm?
		agm.stop(myAid);
	}

}
