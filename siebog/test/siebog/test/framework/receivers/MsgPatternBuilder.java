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

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MsgPatternBuilder {
	private List<MsgField> fields = new ArrayList<>();
	private String fieldName;

	private MsgPatternBuilder() {
	}

	public static MsgPatternBuilder fromFields() {
		return new MsgPatternBuilder();
	}

	public MsgPatternBuilder field(String name) {
		fieldName = name;
		return this;
	}

	public MsgPatternBuilder equalTo(Object value) {
		return withValue(value, false);
	}

	public MsgPatternBuilder matches(String value) {
		return withValue(value, true);
	}

	public MsgPattern build() {
		return new MsgPattern(fields);
	}

	private MsgPatternBuilder withValue(Object value, boolean pattern) {
		if (fieldName == null) {
			throw new IllegalArgumentException("Field name cannot be null.");
		}
		fields.add(new MsgField(fieldName, value, pattern));
		fieldName = null;
		return this;
	}
}
