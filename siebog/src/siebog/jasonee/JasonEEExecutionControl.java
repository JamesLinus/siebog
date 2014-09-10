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

package siebog.jasonee;

import org.w3c.dom.Document;
import jason.control.ExecutionControl;
import jason.control.ExecutionControlInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonEEExecutionControl implements ExecutionControlInfraTier {
	private ExecutionControl userController;

	@Override
	public void informAgToPerformCycle(String agName, int cycle) {
		// TODO Auto-generated method stub
	}

	@Override
	public void informAllAgsToPerformCycle(int cycle) {
		// TODO Auto-generated method stub
	}

	@Override
	public Document getAgState(String agName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}
}
