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

package xjaf2x.server.rest;

/**
 *
 * @author Rade
 */
import java.io.InputStream;
import javax.ws.rs.FormParam;


public class MyMultipartForm {

	@FormParam("file")	
	private InputStream file_input;
	
	@FormParam("masternodeaddress")	
	private String masternodeaddress;
	
	@FormParam("applicationname")	
	private String applicationname;

	/**
	 * @return the masternodeaddress
	 */
	public String getMasternodeaddress() {
		return masternodeaddress;
	}

	/**
	 * @param masternodeaddress the masternodeaddress to set
	 */
	public void setMasternodeaddress(String masternodeaddress) {
		this.masternodeaddress = masternodeaddress;
	}

	/**
	 * @return the applicationname
	 */
	public String getApplicationname() {
		return applicationname;
	}

	/**
	 * @param applicationname the applicationname to set
	 */
	public void setApplicationname(String applicationname) {
		this.applicationname = applicationname;
	}

	public InputStream getFile_input() {
		return file_input;
	}

	public void setFile_input(InputStream file_input) {
		this.file_input = file_input;
	}

}
