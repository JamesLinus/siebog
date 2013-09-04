package org.xjaf2x.server.agents.aco.tsp;

import java.io.Serializable;

/**
 * Represents a single graph vertex.
 */
public class Node implements Serializable
{
	private static final long serialVersionUID = 1L;
	private float x;
	private float y;

	public Node(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
