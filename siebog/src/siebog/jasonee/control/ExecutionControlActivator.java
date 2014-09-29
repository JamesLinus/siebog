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

package siebog.jasonee.control;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;
import org.wildfly.clustering.singleton.SingletonServiceBuilderFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ExecutionControlActivator implements ServiceActivator {
	@Override
	public void activate(ServiceActivatorContext ctx) throws ServiceRegistryException {
		if (ExecutionControlAccessor.serviceActive())
			return; // happens on re-deployment
		ExecutionControlContainer container = new ExecutionControlContainer();
		ExecutionControlService service = new ExecutionControlService(container);

		ServiceController<?> factoryService = ctx.getServiceRegistry().getRequiredService(
				SingletonServiceBuilderFactory.SERVICE_NAME.append("server", "default"));
		SingletonServiceBuilderFactory factory = (SingletonServiceBuilderFactory) factoryService.getValue();
		factory.createSingletonServiceBuilder(ExecutionControlService.NAME, service).build(ctx.getServiceTarget())
				.setInitialMode(ServiceController.Mode.ACTIVE).install();
	}

}
