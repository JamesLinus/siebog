package jade.aco.tsp;

/**
 * Represents a single graph vertex.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Node
{
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