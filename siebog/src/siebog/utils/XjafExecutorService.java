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

package siebog.utils;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.naming.NamingException;
import siebog.core.Global;

/**
 * Wrapper around (managed) executor services.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Startup
@LocalBean
public class XjafExecutorService {
	private static final String NAME = "java:global/" + Global.SERVER + "/" + XjafExecutorService.class.getSimpleName()
			+ "!" + XjafExecutorService.class.getName();
	@Resource(lookup = "java:jboss/ee/concurrency/executor/default")
	private ManagedExecutorService executor;
	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	private ManagedScheduledExecutorService scheduledExecutor;

	public static XjafExecutorService get() throws NamingException {
		return ContextFactory.lookup(NAME, XjafExecutorService.class);
	}

	public Future<?> execute(Runnable task) {
		return executor.submit(task);
	}

	public ScheduledFuture<?> executeRepeating(Runnable task, long delay, long period, TimeUnit unit) {
		return scheduledExecutor.scheduleAtFixedRate(task, delay, period, unit);
	}
}
