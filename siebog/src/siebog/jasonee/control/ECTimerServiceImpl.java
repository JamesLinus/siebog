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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import siebog.utils.GlobalCache;

/**
 * A helper timer service for the Execution control component.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Remote(ECTimerService.class)
@LocalBean
@Startup
@Lock(LockType.READ)
public class ECTimerServiceImpl implements ECTimerService {
	private static final Logger logger = Logger.getLogger(ECTimerServiceImpl.class.getName());
	@Resource
	private TimerService timerService;
	private ECViewListener viewListener;

	@PostConstruct
	public void postConstruct() {
		viewListener = new ECViewListener();
		EmbeddedCacheManager manager = GlobalCache.get().getExecutionControls().getCacheManager();
		manager.addListener(viewListener);
	}

	@PreDestroy
	public void preDestroy() {
		EmbeddedCacheManager manager = GlobalCache.get().getExecutionControls().getCacheManager();
		manager.removeListener(viewListener);
		for (Timer t : timerService.getAllTimers())
			t.cancel();
	}

	@Override
	public void schedule(int delayMillis, String execCtrlName, int cycleNum) {
		final String info = execCtrlName + " " + cycleNum;
		TimerConfig cfg = new TimerConfig(info, false);
		timerService.createSingleActionTimer(delayMillis, cfg);
	}

	@Timeout
	public void timeout(Timer timer) {
		final Cache<String, ExecutionControl> cache = GlobalCache.get().getExecutionControls();
		String info = (String) timer.getInfo();
		try {
			int n = info.indexOf(' ');
			String execCtrlName = info.substring(0, n);
			int cycleNum = Integer.parseInt(info.substring(n + 1));
			ExecutionControl execCtrl = cache.get(execCtrlName);
			if (execCtrl != null)
				execCtrl.onTimeout(cycleNum);
			else
				logger.log(Level.WARNING, "ExecutionControl " + execCtrlName + " not found.");
		} catch (ConcurrentAccessTimeoutException ex) {
			logger.warning(ex.getMessage());
		}
	}
}
