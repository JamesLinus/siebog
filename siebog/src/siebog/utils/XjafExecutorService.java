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
	private static final String REPEAT_CONTENT = "repeat";
	@Resource(lookup = "java:jboss/ee/concurrency/executor/default")
	private ManagedExecutorService executor;
	@Resource(lookup = "java:jboss/ee/concurrency/scheduler/default")
	private ManagedScheduledExecutorService scheduledExecutor;
	private AtomicLong repeatingId = new AtomicLong();
	private Map<Long, ScheduledFuture<?>> repeating = Collections
			.synchronizedMap(new HashMap<Long, ScheduledFuture<?>>());

	public Future<?> execute(Runnable task) {
		return executor.submit(task);
	}

	public long registerHeartbeat(AID aid, long delayMilliseconds) {
		final long id = repeatingId.incrementAndGet();
		final ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.addReceiver(aid);
		msg.setContent(REPEAT_CONTENT);
		final Runnable task = new Runnable() {
			@Override
			public void run() {
				ObjectFactory.getMessageManager().post(msg);
			}
		};
		ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(task, delayMilliseconds, 1,
				TimeUnit.MILLISECONDS);
		repeating.put(id, future);
		return id;
	}

	public boolean isHearbeat(ACLMessage msg) {
		return msg.getPerformative() == Performative.REQUEST && REPEAT_CONTENT.equals(msg.getContentAsString());
	}

	public void cancelRepeating(long id) {
		ScheduledFuture<?> future = repeating.get(id);
		if (future != null) {
			future.cancel(false);
			repeating.remove(id);
		}
	}
}
