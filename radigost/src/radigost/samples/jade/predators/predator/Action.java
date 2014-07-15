package radigost.samples.jade.predators.predator;

public enum Action
{
	UP   ( 0, -1),
	RIGHT( 1,  0),
	DOWN ( 0,  1),
	LEFT (-1,  0),
	STAY ( 0,  0);
	
	private final int dx;
	private final int dy;
	
	Action(int dx, int dy)
	{
		this.dx = dx;
		this.dy = dy;
	}
	
	public final int dx()
	{
		return dx;
	}
	
	public final int dy()
	{
		return dy;
	}
}
