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

package siebog.server.xjaf.utils.deployment;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.wildfly.plugin.deployment.DeploymentExecutionException;
import org.wildfly.plugin.deployment.DeploymentFailureException;
import org.wildfly.plugin.deployment.MatchPatternStrategy;
import org.wildfly.plugin.deployment.domain.Domain;
import org.wildfly.plugin.deployment.domain.DomainDeployment;
import org.wildfly.plugin.deployment.domain.DomainDeployment.Status;
import org.wildfly.plugin.deployment.domain.DomainDeployment.Type;
import siebog.server.xjaf.Global;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Deployment
{
	private static DomainClient createClient(final InetAddress address, final int port)
	{
		final CallbackHandler callbackHandler = new CallbackHandler() {
			@Override
			public void handle(Callback[] callbacks) throws IOException,
					UnsupportedCallbackException
			{
				for (Callback current : callbacks)
					if (current instanceof NameCallback)
					{
						NameCallback ncb = (NameCallback) current;
						ncb.setName(Global.USERNAME);
					} else if (current instanceof PasswordCallback)
					{
						PasswordCallback pcb = (PasswordCallback) current;
						pcb.setPassword(Global.PASSWORD.toCharArray());
					} else
						throw new UnsupportedCallbackException(current);
			}
		};
		return DomainClient.Factory.create(address, port, callbackHandler);
	}

	public static Status deploy(InetAddress host, File file, String name)
			throws DeploymentExecutionException, DeploymentFailureException
	{
		DomainClient client = createClient(host, 9999);
		Domain domain = new Domain(Collections.singletonList(Global.GROUP));
		DomainDeployment deployment = new DomainDeployment(client, domain, file, name,
				Type.FORCE_DEPLOY, null, MatchPatternStrategy.FIRST);
		return deployment.execute();
	}

	public static void main(String[] args) throws UnknownHostException,
			DeploymentExecutionException, DeploymentFailureException
	{
		File f = new File("/home/dejan/dev/siebog/trunk/siebog/siebog.ear");
		Status status = deploy(InetAddress.getByName("localhost"), f, "siebog");
		System.out.println(status);
	}
}
