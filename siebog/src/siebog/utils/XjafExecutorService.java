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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * Wrapper around (managed) executor services.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@LocalBean
public class XjafExecutorService {
	@Resource(lookup = "java:jboss/ee/concurrency/executor/default")
	private ManagedExecutorService executor;
	private AtomicLong hbCounter = new AtomicLong();
	private Set<HeartbeatHandle> heartbeats = Collections.synchronizedSet(new HashSet<HeartbeatHandle>());

	public Future<?> execute(Runnable task) {
		return executor.submit(task);
	}

	public HeartbeatHandle registerHeartbeat(AID aid, long delayMilliseconds) {
		HeartbeatHandle handle = new HeartbeatHandle(hbCounter.incrementAndGet());
		final ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.addReceiver(aid);
		msg.setContent(handle);
		heartbeats.add(handle);
		signalHeartbeat(msg);
		return handle;
	}

	public boolean signalHeartbeat(final ACLMessage msg) {
		HeartbeatHandle handle = (HeartbeatHandle) msg.getContent();
		if (isValidHeartbeatHandle(handle)) {
			execute(new Runnable() {
				@Override
				public void run() {
					ObjectFactory.getMessageManager().post(msg);
				}
			});
			return true;
		}
		return false;
	}

	public boolean isHearbeatMessage(ACLMessage msg) {
		return (msg.getPerformative() == Performative.REQUEST) && (msg.getContent() != null)
				&& (msg.getContent() instanceof HeartbeatHandle);
	}

	public boolean isValidHeartbeatHandle(HeartbeatHandle handle) {
		return handle != null && heartbeats.contains(handle);
	}

	public void cancelHeartbeat(HeartbeatHandle handle) {
		if (handle != null)
			heartbeats.remove(handle);
	}
}
