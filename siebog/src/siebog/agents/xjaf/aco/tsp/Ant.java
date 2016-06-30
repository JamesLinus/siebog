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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.persistence.criteria.CriteriaBuilder.In;

import scala.collection.generic.BitOperations.Int;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.LoggerUtil;

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
	// AID of the master Map agent for this ant.
	private AID mapAID;
	// AID of the Map agent currently processing this ant.
	private AID currentAID;
	//List of all Map agents mantainig the world graph
	private List<AID> maps;
	//Flag true if ant knows all map agents
	private boolean mapsFull = false;
	
	//Begin Node index of master Map
	int beginIndex;
	//End Node index of master Map
	int endIndex;
	
	// Number of nodes on the map (provided by map agent).
	private int mapSize;
	// Index of the current (x,y) pair in the map nodes list.
	private int currentMapPosIndex;
	// Represents all nodes this ant has visited so far (in order of visit).
	private List<Integer> tourSoFar;
	// I-th element represents the weight between i-th and (i+1)-th node in the
	// tourSoFar list.
	private List<Double> tourSoFarWeights;
	// Represents total weight of tourSoFar.
	private double totalWeightSoFar = 0;
	
	
	
	// Local pheromone influence control parameter.
	private final double alpha = 1;
	// Edge weight influence control parameter.
	private final double beta = 5;
	// Pheromone evaporation rate (0 <= ro < 1).
	//private final double ro = 0.1f;
	// Pheromone local evaporation rate (0 <= ksi < 1).
	//private final double ksi = 0.1f;

	// @formatter:off
	/**
	 * 1 - initial tour creation by probabilistic movement (send request) 2 -
	 * while waiting for a reply to the 'PheromoneLevels?' message (process
	 * paths) 3 - while waiting to obtain the weight of the last edge in the
	 * tour. 4 - preparing for backtrack: remove last node on the tourSoFar list
	 * (it is the same as the first) and update the best tour (in the Map agent)
	 * if necessary. 5 - backtrack and pheromone adjustment
	 */
	// @formatter:on
	private int phase;

	// Pheromone local evaporation rate (0 <= ksi < 1).
	private double delta;
	// Space-delimited potential node indices.
	private String potentialNodeIndices;

	private final Random rnd = new Random();

	@Override
	protected void onInit(AgentInitArgs args) {
		String map = args.get("map", null);
		if(map == null)
			map = "Map1";
		mapAID = agm().getAIDByRuntimeName(map);
		currentAID = mapAID;
		maps = new LinkedList<AID>();
		maps.add(mapAID);
		
		ACLMessage message = new ACLMessage();
		message.performative = Performative.REQUEST;
		message.content = "MapSize?";
		message.sender = myAid;
		message.receivers.add(mapAID);
		message.replyWith = "MapSize";
		msm().post(message);
		// LoggerUtil.log(myAid.getName() + ": MapSize? sent.");

	}

	@Override
	protected void onMessage(ACLMessage message) {

		// LoggerUtil.log(myAid.getName() + ": received.",true);
		
		if ("MapSize".equals(message.inReplyTo)) {

			// LoggerUtil.log(myAid.getName() + ": MapSize? received.",true);

			if (message.content.equals("DONE")) {
				agm().stopAgent(myAid);
				return;
			}

			String[] parts = message.content.split("&&");
			String content = parts[0];
			beginIndex = Integer.parseInt(parts[1]) - 1;
			endIndex = Integer.parseInt(parts[2]) - 1;
					
			mapSize = Integer.parseInt(content);
			
			// choose starting map node, from master map, randomly
			currentMapPosIndex = new Random().nextInt(endIndex - beginIndex + 1) + beginIndex;

			tourSoFar = new ArrayList<>();
			tourSoFar.add(currentMapPosIndex);

			tourSoFarWeights = new ArrayList<>();

			ACLMessage request = new ACLMessage(Performative.REQUEST);
			potentialNodeIndices = getPotentialNodeIndices().trim();
			request.content = "Move? " + currentMapPosIndex + " " + potentialNodeIndices;
			request.receivers.add(mapAID);
			request.sender = myAid;
			msm().post(request);
			// LoggerUtil.log(myAid.getName() + ": sent.",true);

			return;
		} else if (message.content.startsWith("Move:")) {
			int newNodeIndex = 0;
			String jsonAID = message.content.split("&&")[1];
			AID newAID = new AID(jsonAID);
			currentAID = newAID;
			if(!mapsFull && !maps.contains(currentAID))
				maps.add(currentAID);
			
			String[] parts = message.content.substring("Move: ".length()).split("&&")[0].split(" ");
			newNodeIndex = Integer.parseInt(parts[0]);
			setCurrentMapPosIndex(newNodeIndex);

			// add new node to tourSoFar
			addNodeToTour(newNodeIndex);

			// add new weight to tourSoFarWeights
			addWeightToTour(Double.parseDouble(parts[1]));
			if(getTourSoFarSize() < getMapSize() + 1){
				sendMoveMessage();
			}else{
				mapsFull = true;
				sendReturnMessage();
			}

		} else if (message.content.startsWith("Restart")) {
			
			// choose starting map node, from master map, randomly
			currentMapPosIndex = new Random().nextInt(endIndex - beginIndex + 1) + beginIndex;
			
			tourSoFar = new ArrayList<>();
			tourSoFar.add(currentMapPosIndex);

			tourSoFarWeights = new ArrayList<>();
			totalWeightSoFar = 0;

			ACLMessage request = new ACLMessage(Performative.REQUEST);
			potentialNodeIndices = getPotentialNodeIndices().trim();
			request.content = "Move? " + currentMapPosIndex + " " + potentialNodeIndices;
			request.receivers.add(mapAID);
			request.sender = myAid;
			msm().post(request);
			// LoggerUtil.log(myAid.getName() + ": sent restartReply.",true);

		} else if (message.content.startsWith("Stop")) {
			agm().stopAgent(myAid);
		}

	}

	public void sendReturnMessage(){
		ACLMessage request = new ACLMessage(Performative.REQUEST);
		StringBuilder tour = new StringBuilder();
		for (int i = 0; i < getTourSoFarSize(); ++i)
			tour.append(" ").append(getTourNode(i));

		request.content = "Returning? " + tour.toString() + "&&" + getTotalWeightSoFar();
		request.receivers.addAll(0, maps);
		request.sender = myAid;
			
		msm().post(request);
	}
	
	public void sendMoveMessage() {
		ACLMessage request = new ACLMessage(Performative.REQUEST);
		if(getTourSoFarSize() == getMapSize()){
			potentialNodeIndices = "" + getFirstMapPosIndex();
		}else{
			potentialNodeIndices = getPotentialNodeIndices().trim();
		}
		

		currentMapPosIndex = getCurrentMapPosIndex();
        
		request.content = "Move? " + currentMapPosIndex + " " + potentialNodeIndices;
		request.receivers.add(currentAID);
		request.sender = myAid;
		
		
		
		msm().post(request);

	}

	public int getTourSoFarSize() {
		return tourSoFar.size();
	}

	public String getTour() {
		String retVal = "";
		for (int i = 0; i < tourSoFar.size(); i++) {
			retVal += tourSoFar.get(i) + " ";
		}
		return retVal.trim();
	}

	public int getMapSize() {
		return mapSize;
	}

	public void addNodeToTour(int nodeIndex) {
		tourSoFar.add(nodeIndex);
	}

	public void addWeightToTour(double weight) {
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

		if (result.toString().trim().equals("")) {
			LoggerUtil.log(" ", true);
			LoggerUtil.log("No potential node indices", true);
			LoggerUtil.log(getTourSoFarSize() + "", true);
			String tour = "";
			for (int s : tourSoFar)
				tour += " " + s;
			LoggerUtil.log(tour, true);
		}
		return result.toString();
	}

	/**
	 * @return index of the map node (in map's 'nodes' list) this ant is
	 *         currently at.
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

	public double getTotalWeightSoFar() {
		return totalWeightSoFar;
	}

	/**
	 * @return Last node of the tourSoFar list, which is subsequently removed,
	 *         or -1, if the list is already empty.
	 */
	public int removeLastNode() {

		if (tourSoFar.size() != 0)
			return tourSoFar.remove(tourSoFar.size() - 1);
		else
			return -1;
	}

	/**
	 * @return Last edge weight of the tourSoFarWeights list, which is
	 *         subsequently removed, or -1, if the list is already empty;
	 */
	public double removeLastWeight() {
		if (tourSoFarWeights.size() != 0)
			return tourSoFarWeights.remove(tourSoFarWeights.size() - 1);
		else
			return -1;
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
	public void onTerminate() {
		LoggerUtil.log("Ant terminated.", true);
	}
}
