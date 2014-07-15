package radigost.server.websocket.bridges;

public class BridgeException extends Throwable
{
	private static final long serialVersionUID = 1L;
	private Throwable cause;
	
	public BridgeException(Throwable cause)
	{
		this.cause = cause;
	}
	
	public Throwable getCause()
	{
		return cause;
	}
}
