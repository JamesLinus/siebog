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
 * 
 * Based on implementations of Centralised and JADE infrastructures 
 * in Jason 1.4.1. 
 * Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al. 
 * 
 * To contact the original authors:
 * http://www.inf.ufrgs.br/~bordini 
 * http://www.das.ufsc.br/~jomi
 */

package siebog.jasonee.control;

import jason.runtime.RuntimeServicesInfraTier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.w3c.dom.Document;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.messagemanager.MessageManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(ExecutionControl.class)
@Lock(LockType.WRITE)
@AccessTimeout(value = 60000, unit = TimeUnit.MILLISECONDS)
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
	private String myName;

	@Override
	public void init(String name, UserExecutionControl userExecCtrl) {
		myName = name;
		registered = new HashSet<>();
		running = new HashSet<>();
		pending = new HashSet<>();
		this.userExecCtrl = userExecCtrl;
		registerTimeout();
	}

	private void registerTimeout() {
		final String name = "ejb:/" + Global.SERVER + "//" + ECTimerServiceImpl.class.getSimpleName() + "!"
				+ ECTimerService.class.getName();
		ECTimerService timer = ObjectFactory.lookup(name, ECTimerService.class);
		int time = userExecCtrl != null ? userExecCtrl.getCycleTimeout() : UserExecutionControl.DEFAULT_TIMEOUT;
		timer.schedule(time, myName, cycleNum);
	}

	@Override
	public void onTimeout(int cycleNum) {
		if (this.cycleNum == cycleNum || cycleNum == -1) {
			filterUnavailableAgents();
			boolean reRegister = !nextCycleIfPossible();
			if (reRegister)
				registerTimeout();
		}
	}

	@Override
	public void informAgToPerformCycle(String agName, int cycle) {
		List<AID> aid = Collections.singletonList(new AID(agName));
		ReasoningCycleMessage msg = new ReasoningCycleMessage(aid, cycle);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAllAgsToPerformCycle(final int cycle) {
		cycleNum = cycle;
		if (pending.size() > 0) {
			registered.addAll(pending);
			pending.clear();
		}
		running.clear();
		running.addAll(registered);
		final ReasoningCycleMessage msg = new ReasoningCycleMessage(registered, cycle);
		if (userExecCtrl != null)
			userExecCtrl.startNewCycle(cycleNum);

		ObjectFactory.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				ObjectFactory.getMessageManager().post(msg);
				registerTimeout();
			}
		});
	}

	@Override
	public void agentCycleFinished(AID aid, boolean isBreakpoint, int cycleNum) {
		if (userExecCtrl != null)
			userExecCtrl.receiveFinishedCycle(aid.toString(), isBreakpoint, cycleNum);
		running.remove(aid);
		nextCycleIfPossible();
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

	@Override
	public void addAgent(AID aid) {
		pending.add(aid);
		nextCycleIfPossible();
	}

	@Override
	public void removeAgent(AID aid) {
		registered.remove(aid);
		pending.remove(aid);
		if (running.remove(aid))
			nextCycleIfPossible();
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return null;
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
			try {
				int successful = msm.post(tm);
				if (successful != 1)
					throw new Exception(); // get() can also throw an exception
			} catch (Exception ex) {
				logger.info("Agent " + aid + " no longer available.");
				i.remove();
				running.remove(aid);
			}
		}
	}
}
