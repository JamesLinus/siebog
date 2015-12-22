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
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.vfs.VirtualFile;

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
	// Initial pheromone value.
	private float tau0;
	// Pheromone values for individual graph edges.
	private float[][] pheromone;

	// Weight of the best tour found so far.
	private float bestTourWeight = Float.MAX_VALUE;
	// Best tour found so far.
	private List<Integer> bestTour = new ArrayList<>();
	// Represents a heuristic for program termination. When this variable reaches
	// MAX_STATIONARY_ITERATIONS, the program terminates.
	private int nIterationsBestTourNotUpdated = 0;
	// Heuristic boundary value for program termination.
	private static final int MAX_STATIONARY_ITERATIONS = 500;
	
	private boolean done = false;

	@Override
	protected void onInit(AgentInitArgs args) {
		LoggerUtil.log("Map opened.", true);
		loadMap(args.get("fileName", null).toString());
	}

	@Override
	protected void onMessage(ACLMessage message) {
		final String content = message.content;
		if (message.performative == Performative.REQUEST) {
			ACLMessage reply = message.makeReply(Performative.INFORM);
			if (content.equals("MapSize?")) {
				if (!done) {
					reply.content = getMapSize() + "";
				} else {
					reply.content = "DONE";
				}
			} else if (content.startsWith("PheromoneLevels?")) {
				String[] parts = content.split(" ");
				StringBuilder pheromoneLevels = new StringBuilder();

				int i = Integer.parseInt(parts[1]);
				pheromoneLevels.append("PheromoneLevels:");
				for (int j = 2; j < parts.length; ++j) {
					int newJ = Integer.parseInt(parts[j]);
					pheromoneLevels.append(" ").append(getPheromoneLevel(i, newJ)).append(" ")
							.append(getEdgeWeight(i, newJ));
				}
				reply.content = pheromoneLevels.toString();
			} else if (content.startsWith("EdgeWeight?")) {
				String[] parts = content.split(" ");
				reply.content = String.valueOf(getEdgeWeight(Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2])));
			}
			msm().post(reply);
		} else if (message.performative == Performative.INFORM) {
			if (content.startsWith("UpdateBestTour")) {
				String[] parts = content.split(" ");
				float newTourWeight = Float.parseFloat(parts[1]);

				LoggerUtil.log("No. of iterations for the best tour not being updated: " + nIterationsBestTourNotUpdated, true);
				if (nIterationsBestTourNotUpdated >= MAX_STATIONARY_ITERATIONS) {
					LoggerUtil.log("Done.", true);
					done = true;
					return;
				}
				nIterationsBestTourNotUpdated++;

				if (bestTourWeight > newTourWeight) {
					nIterationsBestTourNotUpdated = 0;

					bestTourWeight = newTourWeight;

					bestTour.clear();
					for (int i = 2; i < parts.length; ++i)
						bestTour.add(Integer.parseInt(parts[i]) + 1);

					LoggerUtil.log("Best tour so far has weight: " + bestTourWeight, true);
					LoggerUtil.log("Best tour so far: " + bestTour, true);
				}
			} else if (content.startsWith("UpdatePheromone")) {
				String[] parts = content.split(" ");
				int i = Integer.parseInt(parts[1]);
				int j = Integer.parseInt(parts[2]);
				setPheromoneLevel(i, j, Float.parseFloat(parts[3]) * getPheromoneLevel(i, j)
						+ Float.parseFloat(parts[4]));
			} else if (content.startsWith("UpdateLocalPheromone")) {
				String[] parts = content.split(" ");
				int i = Integer.parseInt(parts[1]);
				int j = Integer.parseInt(parts[2]);
				float ksi = Float.parseFloat(parts[3]);
				setPheromoneLevel(i, j, (1 - ksi) * getPheromoneLevel(i, j) + ksi * tau0);
			}
		} else if (message.performative == Performative.CANCEL) {
			LoggerUtil.log("############# Canceled ###############.", true);
			done = true;
		}
	}

	/**
	 * Loads the world graph from the specified file (into 'nodes' list) and calculates initial
	 * pheromone level tau0 which is set for each edge in 'pheromone' matrix.
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
					VirtualFile vf = (VirtualFile)conn.getContent();
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
		LoggerUtil.log("Loading map from: " + f.getAbsolutePath());
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			// skip preliminary info
			for (int i = 0; i < 6; ++i)
				reader.readLine();

			// load the map
			String line = null;
			String[] parts = null;
			while (!(line = reader.readLine()).equals("EOF")) {
				parts = line.split(" ");
				nodes.add(new Node(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
			}

			// print the map
			LoggerUtil.log("Map: ", true);
			StringBuilder sb = new StringBuilder();
			for (Node n : nodes)
				sb.append(n).append(" ");
			LoggerUtil.log(sb.toString(), true);

			// initialize pheromone levels
			int n = nodes.size();
			float C = n * getAverageWeight();
			tau0 = 1 / (n * C);
			pheromone = new float[n][n];
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					pheromone[i][j] = tau0;
		} catch (Exception ex) {
			LoggerUtil.log(ex.getMessage(), true);
		}
	}

	/**
	 * @return average edge weight (Euclid2D) of the currently loaded map.
	 */
	private float getAverageWeight() {
		float result = 0f;
		int n = nodes.size();
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j)
				result += getEdgeWeight(i, j);
		return result / (n * n);
	}

	public Integer getMapSize() {
		return nodes.size();
	}

	/**
	 * @return current pheromone level of the edge between nodes i and j.
	 */
	public float getPheromoneLevel(int i, int j) {
		return pheromone[i][j];
	}

	/**
	 * Set pheromone level of (i,j)-edge to 'val'.
	 */
	public void setPheromoneLevel(int i, int j, float val) {
		pheromone[i][j] = val;
	}

	/**
	 * @return weight of the (i,j) edge (Euclid2D distance between node i and node j).
	 */
	public float getEdgeWeight(int i, int j) {
		Node ni = nodes.get(i);
		Node nj = nodes.get(j);
		return (float) (Math.sqrt(Math.pow(ni.getX() - nj.getX(), 2)
				+ Math.pow(ni.getY() - nj.getY(), 2)));
	}

	@Override
	public void onTerminate() {
		LoggerUtil.log("Map terminated.", true);
	}
}
