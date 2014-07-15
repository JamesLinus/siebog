package radigost.samples.jade.predators.main;

import jade.core.AID;

public class PredatorInfo
{
	private AID id;
	private int x;
	private int y;
	
	public PredatorInfo(AID id)
	{
		this.id = id;
	}
	
	public AID getId()
	{
		return id;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}
}
