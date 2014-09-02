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

package siebog.admin.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class RequestBuilderUtil {
	private static final Logger logger = Logger.getLogger(RequestBuilderUtil.class.getName());
	private static final String TYPE_JSON = "application/json";

	public static boolean get(String url, RequestCallback cb) {
		return makeRequest(RequestBuilder.GET, url, TYPE_JSON, "", cb);
	}

	public static boolean makeRequest(Method method, String url, String contentType, String data, RequestCallback cb) {
		RequestBuilder builder = new RequestBuilder(method, url);
		builder.setHeader("Content-Type", contentType);
		try {
			builder.sendRequest(data, cb);
			return true;
		} catch (RequestException ex) {
			logger.log(Level.WARNING, "Error during " + method + " to " + url, ex);
			return false;
		}
	}
}
