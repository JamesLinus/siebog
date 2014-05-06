package xjaf2x.client.agents.pairs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import xjaf2x.server.agents.pairs.ResultsServiceI;

public class ResultsServiceImpl extends UnicastRemoteObject implements ResultsServiceI
{
	private static final long serialVersionUID = 1L;
	private int numPairs;
	private List<Long> rtts;

	protected ResultsServiceImpl(int numPairs) throws RemoteException
	{
		this.numPairs = numPairs;
		rtts = new ArrayList<>(numPairs);
	}

	@Override
	public synchronized void add(long rtt, String nodeName) throws RemoteException
	{
		rtts.add(rtt);
		System.out.printf("Pair %d from %s done.", rtts.size(), nodeName);
		if (rtts.size() == numPairs)
		{
			long sum = 0;
			for (long l: rtts)
				sum += l;
			System.out.printf("Average RTT: %d ms\n", sum / numPairs);
			System.out.printf("Minimum: %d ms\n", Collections.min(rtts));
			System.out.printf("Maximum: %d ms\n", Collections.max(rtts));
			TimerTask task = new TimerTask() {
				@Override
				public void run()
				{
					System.exit(0);
				}
			};
			new Timer().schedule(task, 1000);
		}
	}

}
