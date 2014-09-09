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

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class ContextFactory {
	private static final Logger logger = Logger.getLogger(ContextFactory.class.getName());
	private static Context context;

	static {
		try {
			Hashtable<String, Object> jndiProps = new Hashtable<>();
			jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			context = new InitialContext(jndiProps);
		} catch (NamingException ex) {
			logger.log(Level.SEVERE, "Context initialization error.", ex);
		}
	}

	public static Context get() {
		return context;
	}

	@SuppressWarnings("unchecked")
	public static <T> T lookup(String name, Class<T> c) {
		try {
			return (T) get().lookup(name);
		} catch (NamingException ex) {
			throw new IllegalStateException("Failed to lookup " + name, ex);
		}
	}
}
