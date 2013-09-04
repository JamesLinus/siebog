package org.xjaf2x.server.agentmanager.agent;

import java.io.Serializable;

// @formatter:off
/**
 * Agent identifier. Includes several pieces of information about the agent:
 * <ul>
 * 		<li>Agent <i>family</i> name: corresponds to a class in OO languages.
 * 		<li><i>Runtime</i> name, specified by the user (e.g. Smith).
 * </ul>
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
// @formatter:on
public final class AID implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String runtimeName;
	private final String family;
	private final String str; // string representation

	public AID(String runtimeName, String family)
	{
		this.runtimeName = runtimeName;
		this.family = family;
		str = family + "/" + runtimeName;
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

	public String getRuntimeName()
	{
		return runtimeName;
	}

	public String getFamily()
	{
		return family;
	}
}
