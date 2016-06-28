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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import siebog.agentmanager.AID;
import siebog.agentmanager.HeartbeatMessage;

/**
 * Wrapper around (managed) executor services.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@LocalBean
public class ExecutorService {
	@Resource(lookup = "java:jboss/ee/concurrency/executor/default")
	private ManagedExecutorService executor;
	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	private ManagedScheduledExecutorService scheduler;
	private AtomicLong hbCounter = new AtomicLong();
	private Map<Long, HeartbeatMessage> heartbeats = Collections.synchronizedMap(new HashMap<Long, HeartbeatMessage>());

	public Future<?> execute(Runnable task) {
		return executor.submit(task);
	}

	public <T> Future<?> execute(final RunnableWithParam<T> task, final T param) {
		return execute(new Runnable() {
			@Override
			public void run() {
				task.run(param);
			}
		});
	}

	public ScheduledFuture<?> execute(Runnable task, long delayMillis) {
		return scheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
	}

	public <T> ScheduledFuture<?> execute(final RunnableWithParam<T> task, long delayMillis, final T param) {
		return execute(new Runnable() {
			@Override
			public void run() {
				task.run(param);
			}
		}, delayMillis);
	}

	public ScheduledFuture<?> schedule(Runnable task, long initialDelayMillis, long periodMillis) {
		return scheduler.scheduleAtFixedRate(task, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
	}

	public <T> ScheduledFuture<?> schedule(final RunnableWithParam<T> task, long initialDelayMillis, long periodMillis,
			final T param) {
		return schedule(new Runnable() {
			@Override
			public void run() {
				task.run(param);
			}
		}, initialDelayMillis, periodMillis);
	}

	public long registerHeartbeat(AID aid, long delayMilliseconds, String content) {
		long handle = hbCounter.incrementAndGet();
		HeartbeatMessage msg = new HeartbeatMessage(aid, handle);
		msg.content = content;
		heartbeats.put(handle, msg);
		signalHeartbeat(handle);
		return handle;
	}

	public boolean signalHeartbeat(long handle) {
		final HeartbeatMessage msg = heartbeats.get(handle);
		if (msg != null) {
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

	public boolean isValidHeartbeatHandle(long handle) {
		return heartbeats.containsKey(handle);
	}

	public void cancelHeartbeat(long handle) {
		heartbeats.remove(handle);
	}
}
