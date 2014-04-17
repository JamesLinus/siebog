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

package xjaf2x.server;

import java.util.Arrays;
import java.util.logging.Logger;
import org.jboss.ejb.client.ClusterNodeSelector;

/**
 * Round-robin cluster node selector (work in progress).
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
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
