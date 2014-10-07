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

import java.io.Serializable;
import siebog.jasonee.control.UserExecutionControl;
import siebog.jasonee.environment.UserEnvironment;

/**
 * Remote interface of the factory object used to create user-defined agent architecture, execution control, and
 * environment. User applications should realize this interface in form of a stateless session EJB. The EJB should be
 * described in the mas2j file as an agent. For example:
 * 
 * <pre>
 * remoteObjectFactory [
 *   module = "siebog", // name of the module/project
 *   object = JasonEEObjectFactory // name of the implementing class 
 * ];
 * </pre>
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface RemoteObjectFactory extends Serializable {
	public static final String NAME = "remoteObjectFactory";

	JasonEEAgArch createAgArch(String className);

	UserExecutionControl createExecutionControl(String className);

	UserEnvironment createEnvironment(String className);
}
