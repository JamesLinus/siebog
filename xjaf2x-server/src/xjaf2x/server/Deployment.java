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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.controller.client.helpers.domain.DeploymentActionResult;
import org.jboss.as.controller.client.helpers.domain.DeploymentActionsCompleteBuilder;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlan;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlanResult;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.DomainDeploymentManager;
import org.jboss.as.controller.client.helpers.domain.DuplicateDeploymentNameException;
import org.jboss.as.controller.client.helpers.domain.ServerGroupDeploymentActionResult;
import org.jboss.as.controller.client.helpers.domain.ServerGroupDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.domain.ServerUpdateResult;
import xjaf2x.Global;

/**
 * Helper class for deploying agents to the JBoss server.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Deployment
{
	private static final Logger logger = Logger.getLogger(Deployment.class.getName());
	private CommandContext ctx;
	private DomainClient client;

	public Deployment(String address) throws Exception
	{
		ctx = CommandContextFactory.getInstance().newCommandContext(address, 9999, Global.USERNAME,
				Global.PASSWORD.toCharArray());
		ctx.connectController();
		client = DomainClient.Factory.create(ctx.getModelControllerClient());
	}

	public Deployment(DomainClient client)
	{
		this.client = client;
	}

	public void disconnect()
	{
		if (ctx != null)
			ctx.disconnectController();
	}

	public boolean deploy(String name, File file)
	{
		DomainDeploymentManager manager = client.getDeploymentManager();
		try
		{
			DeploymentPlan plan = createPlan(manager, true, name, file);
			executePlan(manager, plan);
			return true;
		} catch (Throwable t)
		{
			logger.log(Level.WARNING, "Deployment of agent [" + name + "] failed", t);
			return false;
		}
	}

	public void undeploy(String name)
	{
		DomainDeploymentManager manager = client.getDeploymentManager();
		try
		{
			DeploymentPlan plan = createPlan(manager, false, name, null);
			executePlan(manager, plan);
		} catch (Throwable t)
		{
			logger.log(Level.WARNING, "Undeployment of agent [" + name + "] failed", t);
		}
	}

	private DeploymentPlan createPlan(DomainDeploymentManager manager, boolean deploy, String name,
			File file) throws IOException, DuplicateDeploymentNameException
	{
		DeploymentPlanBuilder builder = manager.newDeploymentPlan();

		DeploymentActionsCompleteBuilder completeBuilder;
		if (deploy)
			completeBuilder = builder.add(name, file).andDeploy();
		else
			completeBuilder = builder.undeploy(name).andRemoveUndeployed();
		ServerGroupDeploymentPlanBuilder groupBuilder = completeBuilder.toServerGroup(Global.GROUP);
		return groupBuilder.withRollback().build();
	}

	private void executePlan(DomainDeploymentManager manager, DeploymentPlan plan) throws Throwable
	{
		if (plan.getDeploymentActions().size() > 0)
		{
			final DeploymentPlanResult planResult = manager.execute(plan).get();
			final Map<UUID, DeploymentActionResult> actionResults = planResult
					.getDeploymentActionResults();
			for (UUID uuid : actionResults.keySet())
			{
				final Map<String, ServerGroupDeploymentActionResult> groupDeploymentActionResults = actionResults
						.get(uuid).getResultsByServerGroup();
				for (String serverGroup2 : groupDeploymentActionResults.keySet())
				{
					final Map<String, ServerUpdateResult> serverUpdateResults = groupDeploymentActionResults
							.get(serverGroup2).getResultByServer();
					for (String server : serverUpdateResults.keySet())
					{
						final Throwable t = serverUpdateResults.get(server).getFailureResult();
						if (t != null)
							throw t;
					}
				}
			}
		}
	}

	/*public static void commandWait()
	{
		// TODO : a hack, until I figure out how to wait for a controlled
		// command to actually finish
		try
		{
			Thread.sleep(5000);
		} catch (InterruptedException e)
		{
		}
	}*/
}
