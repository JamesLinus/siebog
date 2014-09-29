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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ExecutionControlContainer implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Map<String, ExecutionControlBean> controls = new HashMap<>();

	public synchronized void put(String key, ExecutionControlBean value) {
		controls.put(key, value);
		new Thread() {
			@Override
			public void run() {
				while (true) {
					System.out.println("Added, now: " + controls.keySet());
					try {
						Thread.sleep(1500);
					} catch (Exception ex) {
						break;
					}
				}
			}
		}.start();
	}

	public synchronized ExecutionControlBean get(String key) {
		return controls.get(key);
	}

	public synchronized Collection<ExecutionControlBean> getAll() {
		return new ArrayList<ExecutionControlBean>(controls.values());
	}

	public synchronized void stop() {
		controls.clear();
	}

	@Override
	public String toString() {
		return "" + controls.keySet();
	}
}
