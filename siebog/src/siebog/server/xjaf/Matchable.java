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

package siebog.server.xjaf;

/**
 * Implemented by classes whose instances need to be matched against a pattern.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface Matchable<T> {
	/**
	 * Determines whether this object matches the provided pattern. In general, the following
	 * algorithm is used:
	 * 
	 * <pre>
	 * <code>
	 * for (each non-null field f in pattern)
	 *   if (!this.f.matches(pattern.f))
	 *     return false;
	 * return true;
	 * </code>
	 * </pre>
	 * 
	 * @param pattern
	 * @return
	 */
	boolean matches(T pattern);
}
