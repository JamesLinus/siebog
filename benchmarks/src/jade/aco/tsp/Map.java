package jade.aco.tsp;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Map extends Agent
{
	private static final long serialVersionUID = 8207541262514231196L;
	private static final Logger logger = Logger.getLogger(Map.class.getName());
	// TSP graph (given by a set of (x,y)-points).
	private List<Node> nodes;
	// Initial pheromone value.
	private float tau0;
	// Pheromone values for individual graph edges.
	private float[][] pheromone;

	@SuppressWarnings("serial")
	class ProcessMapRequests extends CyclicBehaviour
	{
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
		public void action()
		{
			Map map = ((Map) myAgent);

			ACLMessage msg = map.receive();
			if (msg != null)
			{
				final String content = msg.getContent();
				if (msg.getPerformative() == ACLMessage.REQUEST)
				{
					ACLMessage reply = msg.createReply();

					if (content.equals("MapSize?"))
					{
						reply.setContent(String.valueOf(map.getMapSize()));
					} else if (content.startsWith("PheromoneLevels?"))
					{
						String[] parts = msg.getContent().split(" ");
						StringBuilder pheromoneLevels = new StringBuilder();

						int i = Integer.parseInt(parts[1]);
						pheromoneLevels.append("PheromoneLevels:");
						for (int j = 2; j < parts.length; ++j)
						{
							int newJ = Integer.parseInt(parts[j]);
							pheromoneLevels.append(" ").append(map.getPheromoneLevel(i, newJ))
									.append(" ").append(map.getEdgeWeight(i, newJ));
						}
						reply.setContent(pheromoneLevels.toString());
					} else if (content.startsWith("EdgeWeight?"))
					{
						String[] parts = msg.getContent().split(" ");
						reply.setContent(String.valueOf(map.getEdgeWeight(
								Integer.parseInt(parts[1]), Integer.parseInt(parts[2]))));
					}
					send(reply);
				} else if (msg.getPerformative() == ACLMessage.INFORM)
				{
					if (content.startsWith("UpdateBestTour"))
					{
						String[] parts = msg.getContent().split(" ");
						float newTourWeight = Float.parseFloat(parts[1]);

						nIterationsBestTourNotUpdated++;

						if (bestTourWeight > newTourWeight)
						{
							nIterationsBestTourNotUpdated = 0;

							bestTourWeight = newTourWeight;

							bestTour.clear();
							for (int i = 2; i < parts.length; ++i)
								bestTour.add(Integer.parseInt(parts[i]) + 1);

							if (logger.isLoggable(Level.FINE))
							{
								logger.fine("Best tour so far has weight: " + bestTourWeight);
								logger.fine("Best tour so far: " + bestTour);
							}
						}

						if (nIterationsBestTourNotUpdated == MAX_STATIONARY_ITERATIONS)
							System.exit(0);
					} else if (content.startsWith("UpdatePheromone"))
					{
						String[] parts = content.split(" ");
						int i = Integer.parseInt(parts[1]);
						int j = Integer.parseInt(parts[2]);
						map.setPheromoneLevel(
								i,
								j,
								Float.parseFloat(parts[3]) * map.getPheromoneLevel(i, j)
										+ Float.parseFloat(parts[4]));
					} else if (content.startsWith("UpdateLocalPheromone"))
					{
						String[] parts = content.split(" ");
						int i = Integer.parseInt(parts[1]);
						int j = Integer.parseInt(parts[2]);
						float ksi = Float.parseFloat(parts[3]);
						map.setPheromoneLevel(i, j, (1 - ksi) * map.getPheromoneLevel(i, j) + ksi
								* tau0);
					}
				}
			} else
				block();
		}
	}

	@Override
	protected void setup()
	{
		logger.fine("Map opened.");
		loadMap((String) getArguments()[0]); 
		registerService();
		addBehaviour(new ProcessMapRequests());
	}

	/**
	 * Loads the world graph from the specified file (into 'nodes' list) and calculates initial
	 * pheromone level tau0 which is set for each edge in 'pheromone' matrix.
	 */
	private void loadMap(String fileName)
	{
		nodes = new ArrayList<>();

		InputStream is = getClass().getResourceAsStream("map/" + fileName);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
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

	private void registerService()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("Map");
		sd.setName(getLocalName() + "-MapService");
		dfd.addServices(sd);

		try
		{
			DFService.register(this, dfd);
		} catch (FIPAException fe)
		{
			logger.log(Level.WARNING, "", fe);
		}
	}

	private void deregisterService()
	{
		try
		{
			DFService.deregister(this);
		} catch (FIPAException fe)
		{
			logger.log(Level.WARNING, "", fe);
		}
	}

	public int getMapSize()
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
	protected void takeDown()
	{
		logger.fine("Map closed.");
		deregisterService();
	}
}