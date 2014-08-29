package siebog.radigost.websocket.bridges.jade;

/**
 * FIPA ACL performative constants used by Radigost. Taken from fipaacl.js.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public enum RadigostPerformative
{
	ACCEPT_PROPOSAL(0),
	AGREE(1),
	CANCEL(2),
	CFP(3),
	CONFIRM(4),
	DISCONFIRM(5),
	FAILURE(6),
	INFORM(7),
	INFORM_IF(8),
	INFORM_REF(9),
	NOT_UNDERSTOOD(10),
	PROPOSE(11),
	QUERY_IF(12),
	QUERY_REF(13),
	REFUSE(14),
	REJECT_PROPOSAL(15),
	REQUEST(16),
	REQUEST_WHEN(17),
	REQUEST_WHENEVER(18),
	SUBSCRIBE(19),
	PROXY(20),
	PROPAGATE(21),
	UNKNOWN(-1);
	
	private final int id;
	
	RadigostPerformative(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
}
