package siebog.interaction.contractnet;

import siebog.agents.AID;


/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

public interface FaultCheckingService {
	
	public void createReplyByTimer(AID aid,long duration);
	
	public void createWaitingTimer(AID aid,AID participant,int duration);
	
	public void cancelTimers();
	
	public void cancelTimerWait(long duration, AID participant);

}
