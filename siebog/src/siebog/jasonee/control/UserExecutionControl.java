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

import java.io.Serializable;
import siebog.utils.ObjectFactory;

/**
 * Base interface for user-defined execution control.
 * <p>
 * Execution sequence:
 * <ul>
 * <li>init
 * <li>(receivedFinishedCycle)*
 * <li>stop
 * </ul>
 * </p>
 * Based on jason.control.ExecutionControl
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class UserExecutionControl implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_TIMEOUT = 10000;
	private boolean running = true;
	private String controlName;

	public void init(String controlName, String[] args) {
		this.controlName = controlName;
	}

	public void receiveFinishedCycle(String agName, boolean breakpoint, int cycle) {
	}

	public void stop() {
		running = false;
	}

	public int getCycleTimeout() {
		return DEFAULT_TIMEOUT;
	}

	public void startNewCycle(int cycleNum) {
	}

	public void allAgsFinished(int cycleNum) {
		ExecutionControl control = ObjectFactory.getExecutionControlCache().get(controlName);
		control.informAllAgsToPerformCycle(cycleNum + 1);
	}

	public boolean isRunning() {
		return running;
	}
}
