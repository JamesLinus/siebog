package radigost.samples.jade.predators.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import radigost.samples.jade.predators.Const;
import radigost.samples.jade.predators.msg.MsgContent;
import radigost.samples.jade.predators.msg.Operation;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class MainBehavior extends CyclicBehaviour
{
	private Main main;
	private Map<AID, PredatorInfo> predators;
	private double[] epTime;
	// prey position
	private int px;
	private int py;
	// episode and trial index
	private int epIndex;
	private int triIndex;
	// is there a winner?
	private int predatorMoves;
	private long startTime;
	private boolean first = true;

	public MainBehavior(Main main)
	{
		this.main = main;
		epTime = new double[Const.EP_COUNT];
		// create predators
		predators = new HashMap<AID, PredatorInfo>(Const.NUM_PREDATORS);
		for (int i = 0; i < Const.NUM_PREDATORS; i++)
		{
			String name = "pred" + i;
			AID id = new AID(name, AID.ISLOCALNAME);
			predators.put(id, new PredatorInfo(id));
		}
	}
	
	private void doReset()
	{
		// initial prey pos
		px = (Const.DIM - 1) / 2;
		py = (Const.DIM - 1) / 2;
		// initial positions of predators
		int[] pos = { // x, y
			0, 0,
			Const.DIM - 1, 0,
			Const.DIM - 1, Const.DIM - 1,
			0, Const.DIM - 1
		};
		// (re)initialize each predator
		int i = 0;
		for (PredatorInfo p : predators.values())
		{
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(p.getId());
			MsgContent cont = new MsgContent(Operation.INIT);
			cont.putVar("x", pos[i]);
			cont.putVar("y", pos[i + 1]);
			cont.putVar("dim", Const.DIM);
			cont.putVar("hr", epIndex == 0 ? 1 : 0);
			try
			{
				msg.setContentObject(cont);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			main.send(msg);
		}
		
		predatorMoves = 0;
	}
	
	@Override
	public void action()
	{
		if (first)
		{
			runEpisode();
			first = false;
		}
		
		ACLMessage msg = main.receive();
		if (msg == null)
		{
			block();
			return;
		}
		
		MsgContent cont = null;
		try
		{
			cont = (MsgContent)msg.getContentObject();
		} catch (UnreadableException e)
		{
			e.printStackTrace();
			return;
		}
		switch (cont.getOp())
		{
		case MOVE:
			predatorMoved(msg.getSender(), cont);
			break;
		default:
			break;
		}
	}
	
	private void predatorMoved(AID id, MsgContent cont)
	{
		++predatorMoves;
		// remember new position
		PredatorInfo pred = predators.get(id);
		pred.setX(cont.getVar("x"));
		pred.setY(cont.getVar("y"));
		
		if (predatorMoves % Const.NUM_PREDATORS == 0)
		{
			long tm = System.currentTimeMillis() - startTime;
			for (PredatorInfo pi : predators.values())
			{
				if ((pi.getX() == px) && (pi.getY() == py))
				{
					epTime[epIndex++] += tm;
					// restart episode?
					if (epIndex < Const.EP_COUNT)
						runEpisode();
					else
					{
						if (triIndex % 10 == 0)
							System.out.println("Trial " + triIndex + " done");
						if (++triIndex < Const.TRI_COUNT)
						{
							epIndex = 0;
							runEpisode();
						}
						else
						{
							main.doDelete();
							// print results
							StringBuilder res = new StringBuilder();
							for (int i = 0; i < Const.EP_COUNT; i++)
								res.append((epTime[i] / Const.TRI_COUNT) + "\n");
							System.out.println(res.toString().replace('.', ','));
						}
					}
					return;
				}
			}
			
			// no winner, move the prey
			int newX = px + 1 - (int)(Math.random() * 3);
			int newY = py + 1 - (int)(Math.random() * 3);
			if ((newX >= 0) && (newX < Const.DIM))
				px = newX;
			if ((newY >= 0) && (newY < Const.DIM))
				py = newY;
			// instruct this predator to make a move
			signalMove();
		}
	}
	
	private void runEpisode()
	{
		doReset();
		startTime = System.currentTimeMillis();
		signalMove();
	}
	
	private void signalMove()
	{
		MsgContent cont = new MsgContent(Operation.MOVE);
		cont.putVar("px", px);
		cont.putVar("py", py);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		try
		{
			msg.setContentObject(cont);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		for (PredatorInfo p : predators.values())
			msg.addReceiver(p.getId());
		main.send(msg);
	}
}
