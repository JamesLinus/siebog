package radigost.server.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AgentState
{
	@Id
	private String aid;
	private String state;

	public String getAid()
	{
		return aid;
	}

	public void setAid(String aid)
	{
		this.aid = aid;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}
}
