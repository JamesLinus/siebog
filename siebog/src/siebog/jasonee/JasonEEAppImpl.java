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

import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.infinispan.Cache;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(JasonEEApp.class)
public class JasonEEAppImpl implements JasonEEApp {
	private static final long serialVersionUID = 1L;
	private static Cache<String, JasonEEEnvironment> envs;

	static {
		envs = ObjectFactory.getJasonEEEnvironmentCache();
	}

	@Override
	public JasonEEEnvironment getEnv(String name) {
		return envs.get(name);
	}

	@Override
	public String putEnv(JasonEEEnvironment env) {
		String name = "Env" + System.currentTimeMillis();
		envs.put(name, env);
		return name;
	}
}
