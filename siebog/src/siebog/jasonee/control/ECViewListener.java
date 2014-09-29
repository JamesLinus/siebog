/**
 * Licensed to t
he Apache Software Foundation (ASF) under one 
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

import java.util.HashSet;
import java.util.Set;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Listener
public class ECViewListener {
	@ViewChanged
	public void viewChanged(ViewChangedEvent event) {
		final Cache<String, ExecutionControl> cache = ObjectFactory.getExecutionControlCache();
		Set<String> allExecCtrls = new HashSet<>(cache.keySet());
		for (String name : allExecCtrls) {
			ExecutionControl ctrl = cache.get(name);
			if (ctrl != null)
				ctrl.onTimeout(-1);
		}
	}
}
