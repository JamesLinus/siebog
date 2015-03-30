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

package siebog.test.framework.receivers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import siebog.interaction.ACLMessage;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class BlockingReceiver implements MsgReceiver {
	private ReceiveType type;
	private long timeout;
	private TimeUnit timeUnit;
	private List<MsgPattern> patterns = new ArrayList<>();

	@Override
	public ReceiveType getReceiveType() {
		return type;
	}

	@Override
	public void setReceiveType(ReceiveType type) {
		this.type = type;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}

	@Override
	public Collection<MsgPattern> getMessagePatterns() {
		return patterns;
	}

	@Override
	public void setMessagePatterns(Collection<MsgPattern> patterns) {
		this.patterns = new ArrayList<>(patterns);
	}

	@Override
	public void receive() {
		switch (type) {
		case ANY:
			receiveAny();
			break;
		case ALL_IN_ORDER:
			receiveAllInOrder();
			break;
		case ALL_NO_ORDER:
			receiveAllNoOrder();
			break;
		}
	}

	private void receiveAny() {
	}

	private void receiveAllInOrder() {
	}

	private void receiveAllNoOrder() {
	}
}
