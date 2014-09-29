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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Remote(ECTimerService.class)
@LocalBean
@Startup
public class ECTimerServiceImpl implements ECTimerService {
	private static final Logger logger = Logger.getLogger(ECTimerServiceImpl.class.getName());
	@Resource
	private TimerService timerService;

	@PostConstruct
	public void postConstruct() {
		ScheduleExpression exp = new ScheduleExpression();
		exp.hour("*").minute("*").second("0/30");
		timerService.createCalendarTimer(exp, new TimerConfig("", false));
	}

	@Override
	public void schedule(int delayMillis, String execCtrlName, int cycleNum) {
		final String info = execCtrlName + " " + cycleNum;
		TimerConfig cfg = new TimerConfig(info, false);
		timerService.createSingleActionTimer(delayMillis, cfg);
		// ScheduleExpression exp = new ScheduleExpression();
		// exp.hour("*").minute("*").second("0/" + timeoutSeconds);
		// timer.createCalendarTimer(exp, new TimerConfig(info, false));
	}

	@Timeout
	public void timeout(Timer timer) {
		String info = (String) timer.getInfo();
		if (info.length() == 0) {
			final Collection<String> all = ExecutionControlAccessor.getAll();
			logger.warning("+++++++++++++++++++++++++++++++" + all);
			for (String name : all)
				ExecutionControlAccessor.getExecutionControl(name).onTimeout(-1);
		} else {
			int n = info.indexOf(' ');
			String execCtrlName = info.substring(0, n);
			int cycleNum = Integer.parseInt(info.substring(n + 1));
			ExecutionControl execCtrl = ExecutionControlAccessor.getExecutionControl(execCtrlName);
			if (execCtrl != null)
				execCtrl.onTimeout(cycleNum);
			else
				logger.log(Level.WARNING, "ExecutionControl " + execCtrlName + " not found.");
		}
	}

	@Override
	public void start(Collection<ExecutionControlBean> execCtrls) {
		// this call is needed when the HA service is restored on another node
		// it will kick-start all execution controls
		for (ExecutionControlBean ec : execCtrls)
			ec.registerTimer();
	}

	@Override
	public void stop() {
		for (Timer t : timerService.getAllTimers())
			t.cancel();
	}
}
