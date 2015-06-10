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

package dnars.siebog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.siebog.annotations.Beliefs;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class BeliefParser {
	private DNarsAgent agent;

	public BeliefParser(DNarsAgent agent) {
		this.agent = agent;
	}

	public List<Statement> getInitialBeliefs() {
		Method[] methods = agent.getClass().getDeclaredMethods();
		return getInitialBeliefs(methods);
	}

	private List<Statement> getInitialBeliefs(Method[] methods) {
		List<Statement> beliefs = new ArrayList<>();
		for (Method m : methods) {
			if (m.isAnnotationPresent(Beliefs.class)) {
				parseInitialBeliefs(m, beliefs);
			}
		}
		return beliefs;
	}

	private void parseInitialBeliefs(Method method, List<Statement> beliefs) {
		List<Statement> st = getStatements(method);
		beliefs.addAll(st);
	}

	private List<Statement> getStatements(Method method) {
		checkReturnType(method);
		try {
			return tryGetStatements(method);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalStateException("Error while retrieving initial beliefs.", ex);
		}
	}

	private void checkReturnType(Method method) {
		Class<?> rt = method.getReturnType();
		if (!rt.equals(Statement[].class) && !rt.equals(String[].class)) {
			String msg = String.format("Method %s should return an array of "
					+ "Statement's or String's.", method.getName());
			throw new IllegalStateException(msg);
		}
	}

	private List<Statement> tryGetStatements(Method method) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Object result = method.invoke(agent);
		Statement[] st = castTo(result, Statement[].class);
		if (st == null) {
			String[] str = castTo(result, String[].class);
			st = strToStat(str);
		}
		return Arrays.asList(st);
	}

	private <X> X castTo(Object obj, Class<X> clazz) {
		try {
			return clazz.cast(obj);
		} catch (ClassCastException ex) {
			return null;
		}
	}

	private Statement[] strToStat(String[] str) {
		if (str == null) {
			return null;
		}
		Statement[] res = new Statement[str.length];
		for (int i = 0; i < str.length; i++) {
			res[i] = StatementParser.apply(str[i]);
		}
		return res;
	}
}
