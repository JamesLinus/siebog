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
import java.util.List;
import siebog.interaction.ACLMessage;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MsgPattern {
	private List<MsgField> fields = new ArrayList<>();

	public MsgPattern(List<MsgField> fields) {
		this.fields = fields;
		checkFields();
	}

	public boolean matched(ACLMessage msg) {
		for (MsgField f : fields) {
			try {
				if (!matchesField(msg, f)) {
					return false;
				}
			} catch (NoSuchFieldException | IllegalAccessException ex) {
				throw new IllegalArgumentException(ex);
			}
		}
		return true;
	}

	private void checkFields() {
		for (MsgField f : fields) {
			checkField(f);
		}
	}

	private void checkField(MsgField f) {
		try {
			ACLMessage.class.getField(f.getName());
		} catch (NoSuchFieldException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private boolean matchesField(ACLMessage msg, MsgField f) throws NoSuchFieldException,
			IllegalAccessException {
		java.lang.reflect.Field rf = msg.getClass().getField(f.getName());
		Object value = rf.get(msg);
		if (value == null) {
			return f.getValue() == null;
		}
		if (f.getValue() == null) {
			return false;
		}
		if (f.isPattern()) {
			String expStr = f.getValue().toString();
			return expStr.matches(value.toString());
		}
		return f.getValue().equals(value);
	}
}
