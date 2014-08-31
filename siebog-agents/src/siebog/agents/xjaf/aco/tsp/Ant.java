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

package siebog.agents.xjaf.aco.tsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.agents.xjaf.Module;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * Implementation of an ant.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateful
@Remote(Agent.class)
public class Ant extends XjafAgent {
	private static final long serialVersionUID = 8886978416763257091L;
	private static final Logger logger = Logger.getLogger(Ant.class.getName());
	// AID of the agent maintaining the world graph.
	private AID mapAID;
	// Number of nodes on the map (provided by map agent).
	private int mapSize;
	// Index of the current (x,y) pair in the map nodes list.
	private int currentMapPosIndex;
	// Represents all nodes this ant has visited so far (in order of visit).
	private List<Integer> tourSoFar;
	// I-th element represents the weight between i-th and (i+1)-th node in the tourSoFar list.
	private List<Float> tourSoFarWeights;
	// Represents total weight of tourSoFar.
	private float totalWeightSoFar = 0f;

	// Local pheromone influence control parameter.
	private final float alpha = 1f;
	// Edge weight influence control parameter.
	private final float beta = 5f;
	// Pheromone evaporation rate (0 <= ro < 1).
	private final float ro = 0.1f;
	// Pheromone local evaporation rate (0 <= ksi < 1).
	private final float ksi = 0.1f;

	// @formatter:off
	/**
	 * 1 - initial tour creation by probabilistic movement (send request)
	 * 2 - while waiting for a reply to the 'PheromoneLevels?' message (process paths) 
	 * 3 - while waiting to obtain the weight of the last edge in the tour. 
	 * 4 - preparing for backtrack: remove last node on the tourSoFar list (it is the same as the first) 
	 * and update the best tour (in the Map agent) if necessary. 
	 * 5 - backtrack and pheromone adjustment
	 */
	// @formatter:on
	private int phase;

	// Pheromone local evaporation rate (0 <= ksi < 1).
	private float delta;
	// Space-delimited potential node indices.
	private String potentialNodeIndices;

	private final Random rnd = new Random();

	@Override
	protected void onInit(Map<String, String> args) {
		mapAID = agm.getAIDByRuntimeName("Map");

		ACLMessage message = new ACLMessage();
		message.setPerformative(Performative.REQUEST);
		message.setContent("MapSize?");
		message.setSender(myAid);
		message.addReceiver(mapAID);
		message.setReplyWith("MapSize");
		msm.post(message);
	}

	@Override
	public void onMessage(ACLMessage message) {
		if ("MapSize".equals(message.getInReplyTo())) {
			mapSize = Integer.parseInt(message.getContent());
			// choose starting map node randomly
			currentMapPosIndex = new Random().nextInt(mapSize);

			tourSoFar = new ArrayList<>();
			tourSoFar.add(currentMapPosIndex);

			tourSoFarWeights = new ArrayList<>();

			phase = 1;
			ACLMessage start = new ACLMessage(Performative.REQUEST);
			start.addReceiver(myAid);
			msm.post(start);
			return;
		}

		switch (phase) {
		case 1: {
			ACLMessage request = new ACLMessage(Performative.REQUEST);
			potentialNodeIndices = getPotentialNodeIndices().trim();

			currentMapPosIndex = getCurrentMapPosIndex();

			request.setContent("PheromoneLevels? " + currentMapPosIndex + " " + potentialNodeIndices);
			request.addReceiver(mapAID);
			request.setSender(myAid);
			msm.post(request);

			phase = 2;
			break;
		}
		case 2: {
			int newNodeIndex = 0;

			// response contains a header and alternating numbers designating pheromone level and
			// edge weight for every applicable edge
			String[] parts = ((String) message.getContent()).split(" ");

			// set pheromone and weight hashmaps
			java.util.Map<Integer, Float> pheromones = new HashMap<>();
			java.util.Map<Integer, Float> weights = new HashMap<>();
			float total = 0f;
			for (int i = 1; i < parts.length; i += 2) {
				float weight = Float.parseFloat(parts[i + 1]);
				float pheromoneLevel = Float.parseFloat(parts[i]);
				float val = (float) (Math.pow(pheromoneLevel, alpha) * Math.pow(1 / weight, beta));
				total += val;
				pheromones.put((i + 1) / 2, val);
				weights.put((i + 1) / 2, weight);
			}

			// set probability distribution
			java.util.Map<Integer, Float> probabilities = new HashMap<>();
			for (int i = 1; i <= pheromones.size(); ++i)
				probabilities.put(i, pheromones.get(i) / total);

			// choose next pheromone index using probability distribution
			double random = rnd.nextDouble();
			int i = 1;
			float val = probabilities.get(i);
			while (i < probabilities.size()) {
				if (random < val)
					break;
				else
					val += probabilities.get(++i);
			}

			// update next node index (using chosen pheromone index i, and potentialNodeIndices
			// string)
			String[] segs = potentialNodeIndices.split(" ");
			newNodeIndex = Integer.parseInt(segs[i - 1]);
			setCurrentMapPosIndex(newNodeIndex);

			// add new node to tourSoFar
			addNodeToTour(newNodeIndex);

			// add new weight to tourSoFarWeights
			addWeightToTour(weights.get(i));

			// initiate local pheromone update
			ACLMessage localUpdate = new ACLMessage(Performative.INFORM);
			localUpdate.addReceiver(mapAID);
			localUpdate.setContent("UpdateLocalPheromone " + currentMapPosIndex + " " + newNodeIndex + " " + ksi);

			// advance the phase as required (if tour complete, continue with phase 3, otherwise,
			// repeat phase 1)
			if (getTourSoFarSize() == getMapSize()) {
				int firstMapPosIndex = getFirstMapPosIndex();
				addNodeToTour(firstMapPosIndex);

				ACLMessage edgeWeightReq = new ACLMessage(Performative.REQUEST);
				edgeWeightReq.addReceiver(mapAID);
				edgeWeightReq.setContent("EdgeWeight? " + currentMapPosIndex + " " + firstMapPosIndex);
				edgeWeightReq.setSender(myAid);
				msm.post(edgeWeightReq);

				currentMapPosIndex = firstMapPosIndex;

				phase = 3;
			} else {
				phase = 1;
				msm.post(message);
			}
			break;
		}
		case 3: {
			addWeightToTour(Float.parseFloat((String) message.getContent()));
			phase = 4;
			msm.post(message);
			break;
		}
		case 4: {
			ACLMessage updateBest = new ACLMessage(Performative.INFORM);
			updateBest.addReceiver(mapAID);
			StringBuilder tourSoFar = new StringBuilder();
			for (int i = 0; i < getTourSoFarSize(); ++i)
				tourSoFar.append(" ").append(getTourNode(i));
			float tourWeight = getTotalWeightSoFar();
			updateBest.setContent("UpdateBestTour " + tourWeight + tourSoFar.toString());
			updateBest.setSender(myAid);
			msm.post(updateBest);

			delta = 1 / tourWeight;

			removeLastNode(); // which is the same as the first

			phase = 5;

			msm.post(message);
			break;
		}
		default: // phase == 5
		{
			int nextNodeIndex = removeLastNode();
			if (nextNodeIndex == -1) {
				phase = 6;
				// when this ant is done, create another one
				String name = "Ant" + myAid.hashCode() + System.currentTimeMillis();
				AgentClass agClass = new AgentClass(Module.NAME, "Ant");
				agm.startAgent(agClass, name, null);
				agm.stopAgent(myAid);
				return;
			}

			currentMapPosIndex = getCurrentMapPosIndex();

			ACLMessage updatePheromone = new ACLMessage(Performative.INFORM);
			updatePheromone.addReceiver(mapAID);
			// float val = (1 - ro) * oldValue + ro * delta; // final formula is constructed in Map
			// agent (for simplicity of oldValue retrieval)
			updatePheromone.setContent("UpdatePheromone " + currentMapPosIndex + " " + nextNodeIndex + " " + (1 - ro)
					+ " " + ro * delta);
			updatePheromone.setSender(myAid);
			msm.post(updatePheromone);

			setCurrentMapPosIndex(nextNodeIndex);

			msm.post(message);
		}
		}
	}

	public int getTourSoFarSize() {
		return tourSoFar.size();
	}

	public int getMapSize() {
		return mapSize;
	}

	public void addNodeToTour(int nodeIndex) {
		tourSoFar.add(nodeIndex);
	}

	public void addWeightToTour(float weight) {
		tourSoFarWeights.add(weight);
		totalWeightSoFar += weight;
	}

	/**
	 * @return space-delimited indices of unvisited nodes.
	 */
	public String getPotentialNodeIndices() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < mapSize; ++i)
			if (!tourSoFar.contains(i))
				result.append(" ").append(i);

		return result.toString();
	}

	/**
	 * @return index of the map node (in map's 'nodes' list) this ant is currently at.
	 */
	public int getCurrentMapPosIndex() {
		return currentMapPosIndex;
	}

	/**
	 * Set value as the current map position index.
	 */
	public void setCurrentMapPosIndex(int value) {
		currentMapPosIndex = value;
	}

	/**
	 * @return index of the first node that was visited on the current tour.
	 */
	public int getFirstMapPosIndex() {
		return tourSoFar.get(0);
	}

	public float getTotalWeightSoFar() {
		return totalWeightSoFar;
	}

	/**
	 * @return Last node of the tourSoFar list, which is subsequently removed, or -1, if the list is
	 *         already empty.
	 */
	public int removeLastNode() {

		if (tourSoFar.size() != 0)
			return tourSoFar.remove(tourSoFar.size() - 1);
		else
			return -1;
	}

	/**
	 * @return Last edge weight of the tourSoFarWeights list, which is subsequently removed, or -1,
	 *         if the list is already empty;
	 */
	public float removeLastWeight() {
		if (tourSoFarWeights.size() != 0)
			return tourSoFarWeights.remove(tourSoFarWeights.size() - 1);
		else
			return -1f;
	}

	/**
	 * @return tourSoFar element with index 'index'.
	 */
	public int getTourNode(int index) {
		return tourSoFar.get(index);
	}

	/**
	 * Agent clean-up.
	 */
	@Override
	protected void onTerminate() {
		logger.fine("Ant terminated.");
	}
}
