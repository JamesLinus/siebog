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

import java.util.ArrayList;
import java.util.Collection;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceController;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ExecutionControlAccessor {
	public static ExecutionControl getExecutionControl(String name) {
		return ObjectFactory.getExecutionControlCache().get(name);
	}

	public static String putExecutionControl(ExecutionControl value) {
		final String name = "ExecCtrl" + (int) (Math.random() * 1000000);
		ObjectFactory.getExecutionControlCache().put(name, value);
		return name;
	}

	public static Collection<String> getAll() {
		return ObjectFactory.getExecutionControlCache().keySet();
	}

	public static boolean serviceActive() {
		ServiceController<?> service = CurrentServiceContainer.getServiceContainer().getService(
				ExecutionControlService.NAME);
		return service != null;
	}

	/*
	 * public static ExecutionControlContainer getContainer() { ServiceController<?> service =
	 * CurrentServiceContainer.getServiceContainer().getService( ExecutionControlService.NAME);
	 * return (ExecutionControlContainer) service.getValue(); }
	 */
}
