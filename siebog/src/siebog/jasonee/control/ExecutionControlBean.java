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
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.w3c.dom.Document;
import siebog.jasonee.JasonEERuntimeServices;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.managers.MessageManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(ExecutionControl.class)
public class ExecutionControlBean implements ExecutionControl {
	private static final Logger logger = Logger.getLogger(ExecutionControlBean.class.getName());
	private static final long serialVersionUID = 1L;
	private int cycleNum;
	private Set<AID> registered;
	private Set<AID> running;
	// agents that have registered themselves in the middle of a reasoning cycle.
	// they will be included in the list of all agents at the beginning of the next cycle
	private Set<AID> pending;
	private UserExecutionControl userExecCtrl;
	private String lock = "ec"; // needs to be a serializable object
	private String myName;

	@Override
	public void init(String name, UserExecutionControl userExecCtrl) {
		myName = name;
		registered = new HashSet<>();
		running = new HashSet<>();
		pending = new HashSet<>();
		this.userExecCtrl = userExecCtrl;
		registerTimer();
	}

	public void registerTimer() {
		ECTimerService timer = ExecutionControlService.getTimer();
		int time = userExecCtrl != null ? userExecCtrl.getCycleTimeout() : UserExecutionControl.DEFAULT_TIMEOUT;
		synchronized (lock) {
			timer.schedule(time, myName, cycleNum);
		}
	}

	public void onTimeout(int cycleNum) {
		if (cycleNum == -1)
			logger.warning("---------------------------------YES");
		boolean reRegister = false;
		synchronized (lock) {
			if (this.cycleNum == cycleNum || cycleNum == -1) {
				filterUnavailableAgents();
				reRegister = !nextCycleIfPossible();
			}
		}
		if (reRegister)
			registerTimer();
	}

	@Override
	public void informAgToPerformCycle(String agName, int cycle) {
		List<AID> aid = Collections.singletonList(new AID(agName));
		ReasoningCycleMessage msg = new ReasoningCycleMessage(aid, cycle);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAllAgsToPerformCycle(final int cycle) {
		ReasoningCycleMessage msg;
		synchronized (lock) {
			cycleNum = cycle;
			if (pending.size() > 0) {
				registered.addAll(pending);
				pending.clear();
			}
			running.clear();
			running.addAll(registered);
			msg = new ReasoningCycleMessage(registered, cycle);
			if (userExecCtrl != null)
				userExecCtrl.startNewCycle(cycleNum);
		}

		final ReasoningCycleMessage msgToSend = msg;
		ObjectFactory.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				ObjectFactory.getMessageManager().post(msgToSend);
				registerTimer();
			}
		});
	}

	public void agentCycleFinished(AID aid, boolean isBreakpoint, int cycleNum) {
		synchronized (lock) {
			if (userExecCtrl != null)
				userExecCtrl.receiveFinishedCycle(aid.toString(), isBreakpoint, cycleNum);
			running.remove(aid);
			nextCycleIfPossible();
		}
	}

	private boolean nextCycleIfPossible() {
		boolean canProceed = (registered.size() + pending.size() > 0) && running.size() == 0;
		if (canProceed) {
			if (userExecCtrl != null)
				userExecCtrl.allAgsFinished(cycleNum);
			else
				informAllAgsToPerformCycle(cycleNum + 1);
			return true;
		}
		return false;
	}

	public void addAgent(AID aid) {
		synchronized (lock) {
			pending.add(aid);
			nextCycleIfPossible();
		}
	}

	public void removeAgent(AID aid) {
		synchronized (lock) {
			registered.remove(aid);
			pending.remove(aid);
			if (running.remove(aid))
				nextCycleIfPossible();
		}
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}

	@Override
	public Document getAgState(String agName) {
		// TODO Auto-generated method stub
		return null;
	}

	private void filterUnavailableAgents() {
		final MessageManager msm = ObjectFactory.getMessageManager();
		Iterator<AID> i = registered.iterator();
		while (i.hasNext()) {
			AID aid = i.next();
			ReasoningCycleTimeout tm = new ReasoningCycleTimeout(aid, cycleNum);
			logger.warning("---------------------- sending to " + aid);
			if (msm.post(tm) != 1) {
				logger.info("Agent " + aid + " no longer available.");
				i.remove();
				running.remove(aid);
			}
			logger.warning("--------------------------sent");
		}
	}
}
