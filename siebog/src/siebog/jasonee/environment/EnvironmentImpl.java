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

package siebog.jasonee.environment;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.runtime.RuntimeServicesInfraTier;
import java.util.Collection;
import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.jasonee.ActionFeedbackMessage;
import siebog.jasonee.JasonEERuntimeServices;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Environment.class)
public class EnvironmentImpl implements Environment {
	private static final long serialVersionUID = 1L;
	private UserEnvironment userEnv;

	@Override
	public void init(UserEnvironment userEnv) {
		this.userEnv = userEnv;
	}

	@Override
	public List<Literal> getPercepts(AID aid) {
		return userEnv.getPercepts(aid.toString());
	}

	@Override
	public void scheduleAction(AID aid, Structure action, String replyWith) {
		userEnv.scheduleAction(aid.toString(), action, replyWith);
	}

	@Override
	public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
		ACLMessage msg = new ActionFeedbackMessage(new AID(agName), success, (String) infraData);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAgsEnvironmentChanged(String... agents) {
		ACLMessage msg = new EnvironmentChangedMessage(agents);
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAgsEnvironmentChanged(Collection<String> agents) {
		informAgsEnvironmentChanged(agents.toArray(new String[0]));
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}

	@Override
	public boolean isRunning() {
		return false;
	}

}
