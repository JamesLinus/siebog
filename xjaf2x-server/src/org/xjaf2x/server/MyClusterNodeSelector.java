package org.xjaf2x.server;

import java.util.Arrays;
import java.util.logging.Logger;
import org.jboss.ejb.client.ClusterNodeSelector;

public class MyClusterNodeSelector implements ClusterNodeSelector
{
	private static final Logger logger = Logger.getLogger(MyClusterNodeSelector.class.getName());
	private static int index = -1;
	
	@Override
	public String selectNode(String clusterName, String[] connectedNodes, String[] availableNodes)
	{
		logger.warning("=================================== HERE ====================================");
		logger.warning(String.format("%s\n%s\n%s", clusterName, Arrays.toString(connectedNodes), Arrays.toString(availableNodes)));
		logger.warning("=============================================================================");
		index = (index + 1) % availableNodes.length;
		return availableNodes[index];
	}

}
