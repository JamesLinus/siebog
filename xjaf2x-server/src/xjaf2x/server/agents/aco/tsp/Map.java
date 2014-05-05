package xjaf2x.server.agents.aco.tsp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * Map for Ant Colony Optimization
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateful(name = "xjaf2x_server_agents_aco_tsp_Map")
@Remote(AgentI.class)
public class Map extends Agent
{
	private static final long serialVersionUID = 4998652517108886246L;
	private static final Logger logger = Logger.getLogger(Map.class.getName());
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

	@Override
	protected void onInit(Serializable... args)
	{
		logger.fine("Map opened.");
		loadMap(args[0].toString());
	}

	@Override
	protected void onMessage(ACLMessage message)
	{
		final String content = (String) message.getContent();
		ACLMessage reply = message.makeReply(Performative.INFORM);
		if (message.getPerformative() == Performative.REQUEST)
		{
			if (content.equals("MapSize?"))
			{
				reply.setContent(getMapSize());
			} else if (content.startsWith("PheromoneLevels?"))
			{
				String[] parts = content.split(" ");
				StringBuilder pheromoneLevels = new StringBuilder();

				int i = Integer.parseInt(parts[1]);
				pheromoneLevels.append("PheromoneLevels:");
				for (int j = 2; j < parts.length; ++j)
				{
					int newJ = Integer.parseInt(parts[j]);
					pheromoneLevels.append(" ").append(getPheromoneLevel(i, newJ)).append(" ")
							.append(getEdgeWeight(i, newJ));
				}
				reply.setContent(pheromoneLevels.toString());
			} else if (content.startsWith("EdgeWeight?"))
			{
				String[] parts = content.split(" ");
				reply.setContent(String.valueOf(getEdgeWeight(Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]))));
			}
			msm.post(reply);
		} else if (message.getPerformative() == Performative.INFORM)
		{
			if (content.startsWith("UpdateBestTour"))
			{
				String[] parts = content.split(" ");
				float newTourWeight = Float.parseFloat(parts[1]);

				nIterationsBestTourNotUpdated++;

				if (bestTourWeight > newTourWeight)
				{
					nIterationsBestTourNotUpdated = 0;

					bestTourWeight = newTourWeight;

					bestTour.clear();
					for (int i = 2; i < parts.length; ++i)
						bestTour.add(Integer.parseInt(parts[i]) + 1);
					
					logger.warning("Best tour so far has weight: " + bestTourWeight);
					logger.warning("Best tour so far: " + bestTour);
				}
				if (nIterationsBestTourNotUpdated == MAX_STATIONARY_ITERATIONS)
					logger.warning("Done.");
			} else if (content.startsWith("UpdatePheromone"))
			{
				String[] parts = content.split(" ");
				int i = Integer.parseInt(parts[1]);
				int j = Integer.parseInt(parts[2]);
				setPheromoneLevel(i, j, Float.parseFloat(parts[3]) * getPheromoneLevel(i, j)
						+ Float.parseFloat(parts[4]));
			} else if (content.startsWith("UpdateLocalPheromone"))
			{
				String[] parts = content.split(" ");
				int i = Integer.parseInt(parts[1]);
				int j = Integer.parseInt(parts[2]);
				float ksi = Float.parseFloat(parts[3]);
				setPheromoneLevel(i, j, (1 - ksi) * getPheromoneLevel(i, j) + ksi * tau0);
			}
		}
	}

	/**
	 * Loads the world graph from the specified file (into 'nodes' list) and calculates initial
	 * pheromone level tau0 which is set for each edge in 'pheromone' matrix.
	 */
	private void loadMap(String fileName)
	{
		nodes = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader("/home/dejan/tmp/maps/" + fileName)))
		{
			// skip preliminary info
			for (int i = 0; i < 6; ++i)
				reader.readLine();

			// load the map
			String line = null;
			String[] parts = null;
			while (!(line = reader.readLine()).equals("EOF"))
			{
				parts = line.split(" ");
				nodes.add(new Node(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
			}

			// print the map
			if (logger.isLoggable(Level.FINE))
			{
				logger.fine("Map: ");
				StringBuilder sb = new StringBuilder();
				for (Node n : nodes)
					sb.append(n).append(" ");
				logger.fine(sb.toString());
			}

			// initialize pheromone levels
			int n = nodes.size();
			float C = n * getAverageWeight();
			tau0 = 1 / (n * C);
			pheromone = new float[n][n];
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					pheromone[i][j] = tau0;
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "", ex);
		}
	}

	/**
	 * @return average edge weight (Euclid2D) of the currently loaded map.
	 */
	private float getAverageWeight()
	{
		float result = 0f;
		int n = nodes.size();
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j)
				result += getEdgeWeight(i, j);
		return result / (n * n);
	}

	public Integer getMapSize()
	{
		return nodes.size();
	}

	/**
	 * @return current pheromone level of the edge between nodes i and j.
	 */
	public float getPheromoneLevel(int i, int j)
	{
		return pheromone[i][j];
	}

	/**
	 * Set pheromone level of (i,j)-edge to 'val'.
	 */
	public void setPheromoneLevel(int i, int j, float val)
	{
		pheromone[i][j] = val;
	}

	/**
	 * @return weight of the (i,j) edge (Euclid2D distance between node i and node j).
	 */
	public float getEdgeWeight(int i, int j)
	{
		Node ni = nodes.get(i);
		Node nj = nodes.get(j);
		return (float) (Math.sqrt(Math.pow(ni.getX() - nj.getX(), 2)
				+ Math.pow(ni.getY() - nj.getY(), 2)));
	}

	@Override
	protected void onTerminate()
	{
		logger.fine("Map closed.");
	}
}
