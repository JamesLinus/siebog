package radigost.samples.jade.predators.predator;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import radigost.samples.jade.predators.Const;
import radigost.samples.jade.predators.msg.MsgContent;
import radigost.samples.jade.predators.msg.Operation;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class Predation extends CyclicBehaviour
{
	private Predator predator;
	// my current position
	private int x;
	private int y;
	// map dimension
	private int dim;
	// q-values
	private Map<String, Double> q;
	// e-greedy contant
	private double eg; 
	// info about the previous action
	private boolean hasPrev;
	private String prevStateAct;
	// valid actions in the current step
	private Set<Action> valid;

	public Predation(Predator predator)
	{
		this.predator = predator;
		q = new HashMap<String, Double>();
		valid = EnumSet.noneOf(Action.class);
		hardReset();
	}
	
	private void hardReset()
	{
		eg = 0.4;
		q.clear();
		hasPrev = false;
		prevStateAct = null;
	}
	
	private void getValidActions(int preyX, int preyY)
	{
		valid.clear();
		// if the prey is near by, go directly there
		for (Action a : Action.values())
			if ((x + a.dx() == preyX) && (y + a.dy() == preyY))
			{
				valid.add(a);
				return;
			}
		// return all valid actions
		for (Action a : Action.values())
		{
			int newX = x + a.dx();
			int newY = y + a.dy();
			if ((newX >= 0) && (newX < dim) && (newY >= 0) && (newY < dim))
				valid.add(a);
		}
	}
	
	private void move(int preyX, int preyY)
	{
		getValidActions(preyX, preyY);
		// state description
		int sx = (preyX - x) + dim - 1;
		int sy = (preyY - y) + dim - 1;
		String state = sx + "_" + sy + "_";
		
		Action maxA;
		Action[] aarr = new Action[valid.size()];
		valid.toArray(aarr);
		// select random action?
		if (Math.random() < eg)
		{
			int n = (int)(Math.random() * aarr.length);
			maxA = aarr[n];
		}
		else
		{
			// find the maximum state-action pair
			maxA = aarr[0];
			double mx = getQ(state + maxA);
			
			for (int i = 1; i < aarr.length; i++)
			{
				double val = getQ(state + aarr[i]);
				if (val > mx)
				{
					mx = val;
					maxA = aarr[i];
				}
			}
		}
		eg -= 0.05;
		
		String sa = state + maxA;
		
		// win?
		if ((x == preyX) && (y == preyY))
			q.put(sa, Const.REWARD_WIN);
		
		// update previous Q-value
		if (hasPrev)
		{
			double val = Const.LEARNING_RATE_1 * getQ(prevStateAct) +
				Const.LEARNING_RATE * (Const.REWARD_STEP + Const.DISCOUNT_RATE * getQ(sa));
			q.put(prevStateAct, val);
		}
		
		// remember the state-action pair
		prevStateAct = sa;
		hasPrev = true;
		
		// advance
		x += maxA.dx();
		y += maxA.dy();
	}
	
	@Override
	public void action()
	{
		ACLMessage msg = predator.receive();
		if (msg == null)
		{
			block();
			return;
		}
		MsgContent content = null;
		try
		{
			content = (MsgContent)msg.getContentObject();
		} catch (UnreadableException e)
		{
			e.printStackTrace();
			return;
		}
		
		switch (content.getOp())
		{
		case INIT:
			x = content.getVar("x");
			y = content.getVar("y");
			dim = content.getVar("dim");
			if (content.getVar("hr") == 1)
				hardReset();
			break;
		case MOVE:
			int px = content.getVar("px");
			int py = content.getVar("py");
			move(px, py);
			// send info about the move
			MsgContent cont = new MsgContent(Operation.MOVE);
			cont.putVar("x", x);
			cont.putVar("y", y);
			ACLMessage moveMsg = new ACLMessage(ACLMessage.INFORM);
			moveMsg.setSender(predator.getAID());
			moveMsg.addReceiver(new AID("main", AID.ISLOCALNAME));
			try
			{
				moveMsg.setContentObject(cont);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			predator.send(moveMsg);
			break;
		case STOP:
			break;
		}
	}
	
	private double getQ(String sa)
	{
		Double n = q.get(sa);
		if (n != null)
			return n;
		q.put(sa, 0.0);
		return 0.0;
	}
}
