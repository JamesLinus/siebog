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

package siebog.dnars.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class BeliefParser {
	public static List<Statement> getInitialBeliefs(Object target) {
		List<Statement> beliefs = new ArrayList<>();
		for (Method m : target.getClass().getDeclaredMethods())
			if (m.isAnnotationPresent(Beliefs.class))
				parseInitialBeliefs(target, m, beliefs);
		return beliefs;
	}

	private static void parseInitialBeliefs(Object target, Method m, List<Statement> beliefs) {
		try {
			List<Statement> st = getStatements(target, m);
			beliefs.addAll(st);
		} catch (ClassCastException ex) {
			throw new IllegalStateException("Method " + m.getName()
					+ " should return an array of Statement's or String's.");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalStateException("Cannot invoke method " + m.getName(), ex);
		}
	}

	private static List<Statement> getStatements(Object target, Method m) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Object result = m.invoke(target);
		Statement[] st;
		try {
			st = Statement[].class.cast(result);
		} catch (ClassCastException ex) {
			String[] str = String[].class.cast(result);
			st = processStrings(str);
		}
		return Arrays.asList(st);
	}

	private static Statement[] processStrings(String[] str) {
		if (str == null || str.length == 0)
			return new Statement[0];
		Statement[] res = new Statement[str.length];
		for (int i = 0; i < str.length; i++)
			res[i] = StatementParser.apply(str[i]);
		return res;
	}
}
