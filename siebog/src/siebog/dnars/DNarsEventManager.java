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

package siebog.dnars;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import org.infinispan.Cache;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.Agent;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@LocalBean
public class DNarsEventManager {

	private Cache<Event, Agent> cache;

	@PostConstruct
	public void postConstruct() {
		cache = ObjectFactory.getCacheContainer().getCache("dnars-events");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache dnars-events.");
	}

	public void register(Event event, Agent agent) {
		cache.put(event, agent);
	}

	public void deregister(Event event, Agent agent) {

	}
}
