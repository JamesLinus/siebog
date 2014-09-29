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

package siebog.jasonee.control;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import siebog.core.Global;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ExecutionControlService implements Service<ExecutionControlContainer> {
	public static final ServiceName NAME = ServiceName.JBOSS.append("jasonee", "ExecutionControlService");
	private AtomicBoolean started = new AtomicBoolean(false);
	private final ExecutionControlContainer container;

	public ExecutionControlService(ExecutionControlContainer container) {
		this.container = container;
	}

	@Override
	public ExecutionControlContainer getValue() throws IllegalStateException, IllegalArgumentException {
		if (!started.get())
			throw new IllegalStateException("Service not started.");
		return container;
	}

	@Override
	public void start(StartContext ctx) throws StartException {
		if (!started.compareAndSet(false, true))
			throw new StartException("Already started.");
		new Thread() {
			@Override
			public void run() {
				while (started.get()) {
					System.out.println("------------------HERE! " + container.getAll().size());
					try {
						Thread.sleep(4000);
					} catch (Exception ex) {
						break;
					}
				}
			}
		}.start();
		getTimer().start(container.getAll());
	}

	@Override
	public void stop(StopContext ctx) {
		if (started.compareAndSet(true, false))
			getTimer().stop();
	}

	public static ECTimerService getTimer() {
		final String name = "ejb:/" + Global.SERVER + "//" + ECTimerServiceImpl.class.getSimpleName() + "!"
				+ ECTimerService.class.getName();
		return ObjectFactory.lookup(name, ECTimerService.class);
	}
}
