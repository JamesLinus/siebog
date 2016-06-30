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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.vfs.VirtualFile;

import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.LoggerUtil;

/**
 * Implementation of a map, in form of an agent.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateful
@Remote(Agent.class)
public class Map extends XjafAgent {
	private static final long serialVersionUID = 4998652517108886246L;
	// TSP graph (given by a set of (x,y)-points).
	private List<Node> nodes;
	//Map is responsible for a part of the nodes of the whole grap, represented by nodeIndexes
	private ArrayList<Integer> nodeIndexes;
	//List of all Map agents in the system
	private List<String> mapNames;
	
	// Initial pheromone value.
	private double tau0;
	// Pheromone values for individual graph edges.
	// A single Map agent updates only it's own edges
	private double[][] pheromone;
	private double[][] edgeWeights;
	// Weight of the best tour found so far.
	private double bestTourWeight = Double.MAX_VALUE;
	// Best tour found so far.
	private List<Integer> bestTour = new ArrayList<>();
	// Represents a heuristic for program termination. When this variable
	// reaches
	// MAX_STATIONARY_ITERATIONS, the program terminates.
	private int nIterationsBestTourNotUpdated = 0;
	// Heuristic boundary value for program termination.
	private static final int MAX_STATIONARY_ITERATIONS = 50;

	private static final int MAX_ITERATIONS = 100;
	private int iteration;
	private int stationaryIteration;

	private boolean done = false;

	private final Random rnd = new Random();
	// Local pheromone influence control parameter.
	private final double alpha = 1;
	// Edge weight influence control parameter.
	private final double beta = 5;
	// Pheromone evaporation rate (0 <= ro < 1).
	private final double ro = 0.5;
	// Pheromone local evaporation rate (0 <= ksi < 1).
	private final double ksi = 0.15;

	private double tauMax = 0.5;
	private double tauMin = -0.5;

	private List<AID> antList;
	private List<AID> antsFinished;
	private HashMap<AID, String> antPaths;
	
	//Cluster required
	private int nMaps;
	private int nodesPerMap;
	
	@Override
	protected void onInit(AgentInitArgs args) {
		
		iteration = 0;
		stationaryIteration = 0;
		
		antPaths = new HashMap<AID, String>();
		antList = new LinkedList<AID>();
		antsFinished = new LinkedList<AID>();

		String[] arg = args.get("fileName", null).split("&");
		
			
		String mapName = arg[0];
		
		int nodeStartIndex = -1;
		nMaps = 1;
		nodesPerMap = Integer.MAX_VALUE;
		nodeIndexes = null;
		
		if(arg.length > 1){
			nMaps = Integer.parseInt(arg[1].split(":")[1]);
			nodesPerMap = Integer.parseInt(arg[2].split(":")[1]);
			nodeStartIndex = Integer.parseInt(arg[3].split(":")[1]);
			
			nodeIndexes = new ArrayList<Integer>();
			for(int i = 0; i < nodesPerMap; i++){
				nodeIndexes.add(nodeStartIndex + i);
			}
		}
		
		mapNames = new LinkedList<String>();
		for(int i = 1; i <= nMaps; i++){
			mapNames.add("Map" + i);
		}
		
		loadMap(mapName);
		
		
	}

	@Override
	protected void onMessage(ACLMessage message) {
		//LoggerUtil.log("MAP: received " + message.sender.getName(),true);
		String content = message.content;
		if (message.performative == Performative.REQUEST) {
			ACLMessage reply = message.makeReply(Performative.INFORM);
			if (content.equals("MapSize?")) {
				if (!done) {
					antList.add(message.sender);
					reply.content = getMapSize() + "&&" + nodeIndexes.get(0) + "&&" + nodeIndexes.get(nodeIndexes.size()-1);
				} else {
					reply.content = "DONE";
				}
				msm().post(reply);
				// LoggerUtil.log("MAP: sent MapSize reply " +
				// message.sender.getName(),true);

			}else if (content.startsWith("Move?")){ 
				String[] parts = content.split(" ");
				reply.content = "Move: ";
				int ii = Integer.parseInt(parts[1]);
				int newNodeIndex = 0;
				java.util.Map<Integer, Double> pheromones = new HashMap<>();
				java.util.Map<Integer, Double> weights = new HashMap<>();
				double total = 0;
				for (int i = 2; i < parts.length; i++) {
					int newJ = Integer.parseInt(parts[i]);
					double weight = getEdgeWeight(ii, newJ);

					double pheromoneLevel = getPheromoneLevel(ii, newJ);
					double val = (Math.pow(pheromoneLevel, alpha) * Math.pow(1 / weight, beta));
					total += val;
					pheromones.put(i - 1, val);
					weights.put(i - 1, weight);
				}

				// set probability distribution
				java.util.Map<Integer, Double> probabilities = new HashMap<>();

				for (int i = 1; i <= pheromones.size(); ++i)
					probabilities.put(i, pheromones.get(i) / total);

				// choose next pheromone index using probability distribution
				double random = rnd.nextDouble();
				int i = 1;

				
				double val = probabilities.get(i);
				while (i < probabilities.size()) {
					if (random < val)
						break;
					else
						val += probabilities.get(++i);
				}

				newNodeIndex = Integer.parseInt(parts[i+1]);

				//Update local evaporation
				//setPheromoneLevel(ii, newNodeIndex, (1 - ksi) * getPheromoneLevel(ii,newNodeIndex) + ksi * tau0);
				
				AID mapReciever = nodes.get(newNodeIndex).getMapAgent();
				if(mapReciever == null){
					nodes.get(newNodeIndex).setMapAgent(agm().getAIDByRuntimeName(nodes.get(newNodeIndex).getMapName()));
					 mapReciever = nodes.get(newNodeIndex).getMapAgent();
				}
				reply.content += newNodeIndex + " " + weights.get(i) + "&&" + mapReciever.toString();
				msm().post(reply);
			}else if(content.startsWith("Returning?")){
				String[] parts = content.split("\\?")[1].split("&&");
				String[] path = parts[0].trim().split(" ");
				
				
				double newTourWeight = Double.parseDouble(parts[1]);
				
				antPaths.put(message.sender, parts[0].trim() + "&&" + newTourWeight);
				
				
				if(antPaths.size() == antList.size()*nMaps)
				{
					
					LoggerUtil.log(" ", true);
					LoggerUtil.log(myAid.getName() + "  - Iteration: " + iteration, true);
					LoggerUtil.log(" ", true);
					iteration++;
					
					double[][] deltas = new double[nodes.size()][nodes.size()];
					for (String s : antPaths.values()) //Update pheromones and bestPath
					{
						String[] pathData = s.split("&&");
						path = pathData[0].split(" ");
						newTourWeight = Double.parseDouble(pathData[1]);
						
						if (bestTourWeight > newTourWeight) {
							tauMax = (1/ro)*(1/newTourWeight);
							tauMin = tauMax/(2*nodes.size());
							stationaryIteration = 0;

							bestTourWeight = newTourWeight;

							float weight = 0;
							for (int j = 0; j < path.length - 1; j++)
							{
								weight += getEdgeWeight(Integer.parseInt(path[j]),Integer.parseInt(path[j+1]));
							}
							bestTour.clear();
							for (int j = 0; j < path.length; ++j){	
								bestTour.add(Integer.parseInt(path[j]) + 1);
							}

							
							LoggerUtil.log(myAid.getName() + " - Best tour so far has weight: " + bestTourWeight, true);
							LoggerUtil.log(myAid.getName() + " - Best tour so far: " + bestTour, true);
						}
						
						
						// Update pheromone
						double delta = 10 / newTourWeight;
						int indexI = Integer.parseInt(path[0]);
						int indexJ = Integer.parseInt(path[path.length - 2]); // path.length - 2 is to eliminate last node of the tour which is same as the first// node
						deltas[indexI][indexJ] = deltas[indexI][indexJ] + delta;
						deltas[indexJ][indexI] = deltas[indexJ][indexI] + delta;
						//setPheromoneLevel(indexI, indexJ, (1 - ro) * getPheromoneLevel(indexI, indexJ) + (delta));
						for (int j = path.length - 2; j > 1; j--) {
							indexI = Integer.parseInt(path[j]);
							indexJ = Integer.parseInt(path[j - 1]);
							deltas[indexI][indexJ] = deltas[indexI][indexJ] + delta;
							deltas[indexJ][indexI] = deltas[indexJ][indexI] + delta;
							//setPheromoneLevel(indexI, indexJ,
									//(1 - ro) * getPheromoneLevel(indexI, indexJ) + (delta));
						}
					}
					
					for(int i = 0; i < nodes.size(); i++)
					{
						for(int j = 0; j<nodes.size(); j++)
							setPheromoneLevel(i, j, (1 - ro) * getPheromoneLevel(i, j) + (deltas[i][j]));
					}
					
					stationaryIteration++;
					
					if (iteration >= MAX_ITERATIONS || stationaryIteration >= MAX_STATIONARY_ITERATIONS) {
						LoggerUtil.log(myAid.getName() + " - Done.", true);
						done = true;
						LoggerUtil.log(myAid.getName() + " - Best tour: " + bestTour);
						LoggerUtil.log(myAid.getName() + " - Best tour weight: " + bestTourWeight);
					} else {
						// Restart Ant agents
						// antList.clear();
						antPaths.clear();
						
						ACLMessage restartAnts = new ACLMessage(Performative.INFORM);
						restartAnts.receivers.addAll(antList);
						restartAnts.content = "Restart";
						
						msm().post(restartAnts);
						
					}
					
					
				}
								
				
			} 
		} else if (message.performative == Performative.CANCEL) {
			ACLMessage stopAnts = new ACLMessage(Performative.INFORM);
			stopAnts.receivers.addAll(antList);
			stopAnts.content = "Stop";

			msm().post(stopAnts);
			antList.clear();
			antsFinished.clear();
			antPaths.clear();

			LoggerUtil.log("############# Canceled ###############.", true);
			done = true;
		}
		//LoggerUtil.log("MAP: sent " + message.sender.getName(),true);
	}

	/**
	 * Loads the world graph from the specified file (into 'nodes' list) and
	 * calculates initial pheromone level tau0 which is set for each edge in
	 * 'pheromone' matrix.
	 */
	private void loadMap(String mapName) {
		File f = null;
		nodes = new ArrayList<>();
		URL url = ACOStarter.class.getResource("maps/" + mapName);
		System.out.println(url);
		if (url != null) {
			if (url.toString().startsWith("vfs:/")) {
				try {
					URLConnection conn = new URL(url.toString()).openConnection();
					VirtualFile vf = (VirtualFile) conn.getContent();
					f = vf.getPhysicalFile();
				} catch (Exception ex) {
					ex.printStackTrace();
					f = new File(".");
				}
			} else {
				try {
					f = new File(url.toURI());
				} catch (URISyntaxException e) {
					e.printStackTrace();
					f = new File(".");
				}
			}
		} else {
			f = new File(mapName);
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			// skip preliminary info
			for (int i = 0; i < 6; ++i)
				reader.readLine();

			boolean addIndex = false;
			if(nodeIndexes == null){
				nodeIndexes = new ArrayList<Integer>();
				addIndex=true;
			}
			// load the map
			String line = null;
			String[] parts = null;
			int nodeCounter = 1;
			int mapIndex = 0;
		
			while (!(line = reader.readLine()).equals("EOF")) {
							
				
				if((nodeCounter-1)%nodesPerMap == 0)
					mapIndex++;
				
				
				if(addIndex)
					nodeIndexes.add(nodeCounter);
				
				parts = line.split(" ");
				
				nodes.add(new Node(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), mapNames.get(mapIndex-1)));
				nodeCounter++;
				
			}
			System.out.println();

			// print the map
			LoggerUtil.log("Map: ", true);
			StringBuilder sb = new StringBuilder();
			for (Node n : nodes)
				sb.append(n).append(" ");
			LoggerUtil.log(sb.toString(), true);

			// initialize pheromone levels
			int n = nodes.size();
			
			pheromone = new double[n][n];
			edgeWeights = new double[n][n];
						
			double C = n * getAverageWeight();
			//tau0 = 1 / (n * C);
			LoggerUtil.log("" + tau0, true);
			tau0=100;
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j){
					if(j==i)
						pheromone[i][j] = 0;
					else
						pheromone[i][j] = tau0;
				}
		} catch (Exception ex) {
			LoggerUtil.log(ex.getMessage(), true);
		}
	}

	/**
	 * @return average edge weight (Euclid2D) of the currently loaded map.
	 */
	private double getAverageWeight() {
		double result = 0;
		int n = nodes.size();
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j){
				Node ni = nodes.get(i);
				Node nj = nodes.get(j);
				
				edgeWeights[i][j] = (double) (Math.sqrt(Math.pow(ni.getX() - nj.getX(), 2) + Math.pow(ni.getY() - nj.getY(), 2))); getEdgeWeight(i, j);
				result += edgeWeights[i][j];
			}
		return result / (n * n);
	}

	public Integer getMapSize() {
		return nodes.size();
	}

	/**
	 * @return current pheromone level of the edge between nodes i and j.
	 */
	public double getPheromoneLevel(int i, int j) {
		return pheromone[i][j];
	}

	/**
	 * Set pheromone level of (i,j)-edge to 'val'.
	 */
	public void setPheromoneLevel(int i, int j, double val) {
//		if (val > tauMax)
//			val = tauMax;
//		else if (val < tauMin)
//			val = tauMin;

		pheromone[i][j] = val;
		//pheromone[j][i] = val;
	}

	/**
	 * @return weight of the (i,j) edge (Euclid2D distance between node i and
	 *         node j).
	 */
	public double getEdgeWeight(int i, int j) {
//		Node ni = nodes.get(i);
//		Node nj = nodes.get(j);
//		return (double) (Math.sqrt(Math.pow(ni.getX() - nj.getX(), 2) + Math.pow(ni.getY() - nj.getY(), 2)));
		return edgeWeights[i][j];
	}

	@Override
	public void onTerminate() {
		LoggerUtil.log("Map terminated.", true);
	}
}
