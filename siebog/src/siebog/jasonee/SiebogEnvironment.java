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

import java.util.Arrays;
import java.util.Collection;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.MessageManager;
import jason.asSyntax.Structure;
import jason.environment.EnvironmentInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SiebogEnvironment implements EnvironmentInfraTier {
	private static final MessageManager msm = ObjectFactory.getMessageManager();
	private boolean running = true;

	@Override
	public void informAgsEnvironmentChanged(String... agents) {
		informAgsEnvironmentChanged(Arrays.asList(agents));
	}

	@Override
	public void informAgsEnvironmentChanged(Collection<String> agents) {
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		for (String str : agents)
			msg.addReceiver(new AID(str));
		msg.setContent("environmentChanged");
		msm.post(msg);
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new SiebogRuntimeServices();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
		ACLMessage msg = (ACLMessage) infraData;
		msg.setContent("" + success);
		msm.post(msg);
	}
}
