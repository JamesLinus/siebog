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

package siebog.server.xjaf.agents.base;

import java.io.Serializable;
import siebog.server.xjaf.Global;

// @formatter:off
/**
 * Agent identifier. Includes several pieces of information about the agent:
 * <ul>
 * 		<li>Module name (i.e. name of the project).
 * 		<li>EJB name.
 * 		<li>Runtime name.
 * </ul>
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
// @formatter:on
public final class AID implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String name;
	private final String hap;
	private final String str; // string representation

	public AID(String name)
	{
		this(name, Global.getNodeName());
	}
	
	public AID(String name, String hap)
	{
		this.name = name;
		this.hap = hap;
		str = name + "@" + hap;
	}

	@Override
	public int hashCode()
	{
		return str.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AID other = (AID) obj;
		return str.equals(other.str);
	}

	@Override
	public String toString()
	{
		return str;
	}

	public String getName()
	{
		return name;
	}

	public String getHap()
	{
		return hap;
	}
}
