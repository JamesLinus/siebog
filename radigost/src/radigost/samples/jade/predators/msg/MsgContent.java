package radigost.samples.jade.predators.msg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class MsgContent implements Serializable
{
	private Operation op;
	private Map<String, Integer> vars;
	
	public MsgContent(Operation op)
	{
		this.op = op;
		vars = new HashMap<String, Integer>();
	}
	
	public Operation getOp()
	{
		return op;
	}
	
	public int getVar(String id)
	{
		return vars.get(id);
	}
	
	public void putVar(String id, int val)
	{
		vars.put(id, val);
	}
}
