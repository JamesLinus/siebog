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

package siebog.server.xjaf.utils.deployment;

/**
 *
 * @author Rade
 */
import java.io.InputStream;
import javax.ws.rs.FormParam;

public class MyMultipartForm
{
	@FormParam("file")
	private InputStream fileInput;

	@FormParam("masternodeaddress")
	private String masterNodeAddress;

	@FormParam("applicationname")
	private String applicationName;

	public String getMasterNodeAddress()
	{
		return masterNodeAddress;
	}

	public void setMasternodeaddress(String masternodeaddress)
	{
		this.masterNodeAddress = masternodeaddress;
	}

	public String getApplicationName()
	{
		return applicationName;
	}

	public void setApplicationName(String applicationName)
	{
		this.applicationName = applicationName;
	}

	public InputStream getFileInput()
	{
		return fileInput;
	}

	public void setFileInput(InputStream fileInput)
	{
		this.fileInput = fileInput;
	}

}
