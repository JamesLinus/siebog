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

package siebog.agents.xjaf.pso;

import java.util.Map;
import java.util.Random;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * Implementation of a Particle (Particle Swarm Optimization)
 * 
 * @author <a href="mailto:simic.dragan@hotmail.com">Dragan Simic</a>
 */
@Stateful
@Remote(Agent.class)
public class Particle extends XjafAgent {

	private static final long serialVersionUID = -4667142176673603367L;

	/**
	 * Agent ID of a Swarm agent
	 */
	private AID swarmAID;

	/**
	 * Current Particle position
	 */
	private double[] position;

	/**
	 * Fitness of the current position
	 */
	private double fitness;

	/**
	 * Current velocity
	 */
	private double[] velocity;

	/**
	 * Previous position of the Particle that had the best fitness
	 */
	private double[] bestPosition;

	/**
	 * Best fitness of the previous Particle's positions
	 */
	private double bestFitness;

	/**
	 * Dimension of the solution (dependent on the {@link #objectiveFunction(double[])})
	 */
	private int dimension;

	private Random random = null;

	// range for X values
	private double minX;
	private double maxX;

	// range for velocity (calculated based on maxX)
	private double minV;
	private double maxV;

	// inertia weight
	private double w = 0.729;
	// cognitive weight
	private double c1 = 1.49445;
	// social weight
	private double c2 = 1.49445;
	// randomizations
	private double r1, r2;

	/**
	 * @see XjafAgent.server.agm.Agent#onInit(java.io.Serializable[])
	 */
	@Override
	protected void onInit(Map<String, String> args) {

		// read arguments
		dimension = Integer.parseInt(args.get("dimension"));
		minX = Double.parseDouble(args.get("minx"));
		maxX = Double.parseDouble(args.get("maxx"));

		// initialize variables
		minV = -1.0 * maxX;
		maxV = maxX;

		random = new Random();

		// calculate random initial values
		double[] randomPosition = new double[dimension];
		for (int j = 0; j < randomPosition.length; ++j) {
			double lo = minX;
			double hi = maxX;
			randomPosition[j] = (hi - lo) * random.nextDouble() + lo;
		}
		double randomFitness = objectiveFunction(randomPosition);
		double[] randomVelocity = new double[dimension];

		for (int j = 0; j < randomVelocity.length; ++j) {
			double lo = -1.0 * Math.abs(maxX - minX);
			double hi = Math.abs(maxX - minX);
			randomVelocity[j] = (hi - lo) * random.nextDouble() + lo;
		}

		// initialize Particle with random values
		fitness = randomFitness;
		position = new double[dimension];
		System.arraycopy(randomPosition, 0, position, 0, dimension);
		velocity = new double[dimension];
		System.arraycopy(randomVelocity, 0, velocity, 0, dimension);

		// initial values are the best values
		bestPosition = new double[dimension];
		System.arraycopy(position, 0, bestPosition, 0, dimension);
		bestFitness = fitness;

		// get the reference to the Starter (main) PSO agent
		AID swarmAID = agm.getAIDByName("Swarm");

		// compose the message
		ACLMessage message = new ACLMessage();
		message.setPerformative(Performative.REQUEST);
		PsoMessage psoMessage = new PsoMessage(PsoMessage.UPDATE_GLOBAL_SOLUTION, bestFitness, bestPosition);
		message.setContent(psoMessage.toString());
		message.setSender(myAid);
		message.addReceiver(swarmAID);

		// post the message
		msm.post(message);

	}

	/**
	 * Handles incoming messages.
	 * 
	 * @see XjafAgent.server.agm.Agent#onMessage(xjaf2x.server.msm.fipa.acl.ACLMessage)
	 */
	@Override
	protected void onMessage(ACLMessage message) {

		PsoMessage psoMessage = PsoMessage.valueOf(message.getContent());

		if (message.getPerformative() == Performative.REQUEST) {
			if (psoMessage.getAction().equals(PsoMessage.ITERATE_PARTICLE)) {

				// logger.warning("Particle [" + myAid + "] got the iterate request" );
				iteration(psoMessage.getFitness(), psoMessage.getPosition());

				// reply to the swarm that the iteration is finished
				ACLMessage reply = message.makeReply(Performative.INFORM);
				reply.setSender(myAid);
				message.addReceiver(swarmAID);
				msm.post(reply);
			}
		}
	}

	/**
	 * Represents one iteration of a Particle. <br>
	 * Calculates new velocity, position and fitness, and updates local and global best position and
	 * fitness if necessary.
	 * 
	 * @param bestGlobalFitness
	 * @param bestGlobalPosition
	 */
	private void iteration(double bestGlobalFitness, double[] bestGlobalPosition) {

		double[] newVelocity = new double[dimension];
		double[] newPosition = new double[dimension];
		double newFitness;

		// calculate new velocity
		for (int j = 0; j < velocity.length; ++j) {

			r1 = random.nextDouble();
			r2 = random.nextDouble();

			// main calculation of the PSO
			newVelocity[j] = (w * velocity[j]) + (c1 * r1 * (bestPosition[j] - position[j]))
					+ (c2 * r2 * (bestGlobalPosition[j] - position[j]));

			if (newVelocity[j] < minV) {
				newVelocity[j] = minV;
			} else if (newVelocity[j] > maxV) {
				newVelocity[j] = maxV;
			}
		}
		// set new velocity
		System.arraycopy(newVelocity, 0, velocity, 0, dimension);

		// calculate new position
		for (int j = 0; j < position.length; ++j) {
			newPosition[j] = position[j] + newVelocity[j];
			if (newPosition[j] < minX) {
				newPosition[j] = minX;
			} else if (newPosition[j] > maxX) {
				newPosition[j] = maxX;
			}
		}
		// set new position
		System.arraycopy(newPosition, 0, position, 0, dimension);

		// calculate new fitness
		newFitness = objectiveFunction(newPosition);

		// set new fitness
		fitness = newFitness;

		// log line for debugging
		logger.warning("Particle [" + myAid + "] iteration fitness: " + fitness);

		// update local best fitness if necessary
		if (newFitness < bestFitness) {
			System.arraycopy(newPosition, 0, bestPosition, 0, dimension);
			bestFitness = newFitness;
		}

		// update global best fitness if necessary
		if (newFitness < bestGlobalFitness) {
			ACLMessage message = new ACLMessage();
			message.setPerformative(Performative.REQUEST);
			PsoMessage psoMessage = new PsoMessage(PsoMessage.UPDATE_GLOBAL_SOLUTION, newFitness, newPosition);
			message.setContent(psoMessage.toString());
			message.setSender(myAid);
			message.addReceiver(swarmAID);
			msm.post(message);
		}
	}

	/**
	 * function to solve (minimize)
	 * 
	 * @param x potential solutions (array[dimension])
	 * @return result (fitness)
	 */
	private double objectiveFunction(double[] x) {
		return 3.0 + (x[0] * x[0]) + (x[1] * x[1]);
	}

}
