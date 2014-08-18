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

package siebog.server.xjaf.base;

import java.io.Serializable;
import siebog.server.xjaf.Global;

/**
 * Agent identifier, consists of the runtime name and the platform identifier, in the form of
 * "name@hap".
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public final class AID implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String name;
	private final String hap;
	private final String str; // string representation

	/**
	 * Accepts a string in the form of "name@hap", or just "name", in which case the current host's
	 * name will be used as hap.
	 * 
	 * @param nameHap
	 */
	public AID(String nameHap)
	{
		int n = nameHap.indexOf('@');
		if (n == -1)
		{
			name = nameHap;
			hap = Global.getNodeName();
			str = name + '@' + hap;
		} else if (n > 0 && n < nameHap.length() - 1)
		{
			name = nameHap.substring(0, n);
			hap = nameHap.substring(n + 1);
			str = nameHap;
		}
		else
			throw new IllegalArgumentException("Name and HAP cannot be empty.");
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
