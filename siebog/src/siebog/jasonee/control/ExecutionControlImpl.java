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

import jason.runtime.RuntimeServicesInfraTier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.w3c.dom.Document;
import siebog.jasonee.JasonEERuntimeServices;
import siebog.utils.ObjectFactory;
import siebog.utils.RunnableWithParam;
import siebog.xjaf.core.AID;
import siebog.xjaf.managers.AgentManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(ExecutionControl.class)
@Lock(LockType.WRITE)
public class ExecutionControlImpl implements ExecutionControl {
	private static final long serialVersionUID = 1L;
	private int cycleNum;
	private Set<AID> agents;
	private Set<AID> finished;
	// agents that have registered themselves in the middle of a reasoning cycle
	// they will be included in the list of all agents at the beginning of the next cycle
	private Set<AID> pendingAgents;
	private UserExecutionControl userExecCtrl;
	private boolean runningCycle;

	@Override
	public void init(UserExecutionControl userExecCtrl) {
		agents = new HashSet<>();
		finished = new HashSet<>();
		pendingAgents = new HashSet<>();
		this.userExecCtrl = userExecCtrl;
		informAllAgsToPerformCycle(0);
	}

	@Override
	public void informAgToPerformCycle(String agName, int cycle) {
		List<AID> aid = Collections.singletonList(new AID(agName));
		ReasoningCycleMessage msg = new ReasoningCycleMessage(aid, cycle);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAllAgsToPerformCycle(int cycle) {
		runningCycle = true;
		cycleNum = cycle;
		finished.clear();
		if (pendingAgents.size() > 0) {
			agents.addAll(pendingAgents);
			pendingAgents.clear();
		}
		if (userExecCtrl != null)
			userExecCtrl.startNewCycle(cycleNum);
		ReasoningCycleMessage msg = new ReasoningCycleMessage(agents, cycle);
		ObjectFactory.getMessageManager().post(msg);
		scheduleTimeout();
	}

	@Override
	public Document getAgState(String agName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void agentCycleFinished(AID aid, boolean isBreakpoint, int cycleNum) {
		if (userExecCtrl != null)
			userExecCtrl.receiveFinishedCycle(aid.toString(), isBreakpoint, cycleNum);
		finished.add(aid);
		advanceIfDone();
	}

	private boolean advanceIfDone() {
		if (cycleDone()) {
			runningCycle = false;
			if (userExecCtrl != null)
				userExecCtrl.allAgsFinished(cycleNum);
			else
				informAllAgsToPerformCycle(cycleNum + 1);
			return true;
		}
		return false;
	}

	private boolean cycleDone() {
		return !runningCycle || (finished.size() >= agents.size() && finished.containsAll(agents));
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}

	@Override
	public void addAgent(AID aid) {
		pendingAgents.add(aid);
		advanceIfDone(); // needed sometimes, e.g. if this is the very first agent
	}

	@Override
	public void removeAgent(AID aid) {
		agents.remove(aid);
		advanceIfDone();
	}

	private void scheduleTimeout() {
		long timeout;
		if (userExecCtrl != null)
			timeout = userExecCtrl.getCycleTimeout();
		else
			timeout = UserExecutionControl.DEFAULT_TIMEOUT;
		if (timeout > 0) {
			RunnableWithParam<Integer> task = new RunnableWithParam<Integer>() {
				@Override
				public void run(Integer param) {
					if (cycleNum == param) {
						filterUnavailableAgents();
						if (!advanceIfDone())
							scheduleTimeout();
					}
				}
			};
			ObjectFactory.getExecutorService().execute(task, timeout, cycleNum);
		}
	}

	private void filterUnavailableAgents() {
		final AgentManager agm = ObjectFactory.getAgentManager();
		Iterator<AID> i = agents.iterator();
		while (i.hasNext()) {
			AID aid = i.next();
			try {
				agm.pingAgent(aid);
			} catch (IllegalStateException ex) {
				i.remove();
			}
		}
	}
}
