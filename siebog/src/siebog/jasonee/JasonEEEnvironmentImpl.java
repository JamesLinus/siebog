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

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.runtime.RuntimeServicesInfraTier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(JasonEEEnvironment.class)
public class JasonEEEnvironmentImpl implements JasonEEEnvironment {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JasonEEEnvironmentImpl.class.getName());
	private Environment userEnv;

	@Override
	public void init(String userEnvClass, String[] userEnvParams) {
		try {
			userEnv = (Environment) Class.forName(userEnvClass).newInstance();
			userEnv.setEnvironmentInfraTier(this);
			userEnv.init(userEnvParams);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
			logger.log(Level.WARNING, "Unable to instantiate user environment " + userEnvClass, ex);
			userEnv = new Environment();
			userEnv.setEnvironmentInfraTier(this);
		}
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
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.addReceiver(new AID(agName));
		msg.setInReplyTo((String) infraData);
		msg.setContent(new ScheduledActionResult(success));
		ObjectFactory.getMessageManager().post(msg);
	}

	@Override
	public void informAgsEnvironmentChanged(String... agents) {
		informAgsEnvironmentChanged(Arrays.asList(agents));
	}

	@Override
	public void informAgsEnvironmentChanged(Collection<String> agents) {
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
