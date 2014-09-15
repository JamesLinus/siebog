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

import jason.runtime.RuntimeServicesInfraTier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.w3c.dom.Document;
import siebog.jasonee.intf.JasonEEExecutionControl;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(JasonEEExecutionControl.class)
@Lock(LockType.WRITE)
public class JasonEEExecutionControlImpl implements JasonEEExecutionControl {
	private static final long serialVersionUID = 1L;
	private Set<AID> agents;
	private UserExecutionControl userExecCtrl;

	@Override
	public void init(UserExecutionControl userExecCtrl) {
		agents = new HashSet<>();
		this.userExecCtrl = userExecCtrl;
	}

	@Override
	public void informAgToPerformCycle(String agName, int cycle) {
		List<AID> aid = Collections.singletonList(new AID(agName));
		ReasoningCycleMessage msg = new ReasoningCycleMessage(aid, cycle);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAllAgsToPerformCycle(int cycle) {
		ReasoningCycleMessage msg = new ReasoningCycleMessage(agents, cycle);
		ObjectFactory.getMessageManager().post(msg);
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
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}

	@Override
	public void addAgent(AID aid) {
		agents.add(aid);
	}

	@Override
	public void remAgent(AID aid) {
		agents.remove(aid);
	}
}
