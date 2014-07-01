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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import xjaf2x.server.Deployment;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

/**
 * 
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */

@Path("/")
public class RESTws
{
	private static final Logger logger = Logger.getLogger(RESTws.class.getName());

	@POST
	@Consumes("multipart/form-data")
	@Path("/deployagent")
	public Response deployAgent(@MultipartForm MyMultipartForm form)
	{
		String output;
		try
		{
			URL location = RESTws.class.getProtectionDomain().getCodeSource().getLocation();
			System.out.println(location.getFile());
			String folderurl = location.toString().substring(5) + "/tmp/";
			String fileName = folderurl + form.getMasternodeaddress() + "_"
					+ form.getApplicationname() + ".jar";
			saveFile(form.getFile_input(), folderurl, fileName);
			output = "File saved to server location : " + fileName;
			File file = new File(fileName);
			Deployment deployment = new Deployment(form.getMasternodeaddress());
			deployment.deploy(form.getApplicationname(), file);
			return Response.status(200).entity(output).build();
		} catch (Exception e)
		{
			output = "Error";
			logger.log(Level.INFO, "Error while deploying agent.", e);
			return Response.status(400).entity(output).build();
		}

	}

	private void saveFile(InputStream uploadedInputStream, String folderurl, String serverLocation)
	{

		try
		{
			new File(folderurl).mkdirs();

			OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1)
			{
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e)
		{
			logger.log(Level.INFO, "Error while saving file - [" + folderurl + "] .", e);
		}
	}

}
