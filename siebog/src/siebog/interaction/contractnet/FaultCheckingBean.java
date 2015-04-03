package siebog.interaction.contractnet;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentManagerBean;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

@Singleton
@Startup
@Remote(FaultCheckingService.class)
public class FaultCheckingBean implements FaultCheckingService {
	private static final Logger LOG = LoggerFactory
			.getLogger(FaultCheckingBean.class);

	@Resource
	TimerService timerService;

	@Inject
	private AgentManagerBean agm;

	@PreDestroy
	public void preDestroy() {
		cancelTimers();
	}

	@Override
	public void createReplyByTimer(AID aid, long duration) {

		timerService.createTimer(duration, aid + ";ReplyBy");
	}

	@Override
	public void createWaitingTimer(AID aid, AID participant, int duration) {
		timerService.createTimer(duration * 1000, aid + ";ProposedValue;"
				+ duration + ";" + participant);
	}

	@Override
	public void cancelTimers() {
		try {
			LOG.info("Cancelling {} timers.", timerService.getTimers().size());
			for (Object obj : timerService.getTimers()) {
				Timer timer = (Timer) obj;
				timer.cancel();
				LOG.info("Cancelled timer {}.", timer.getInfo());
			}
		} catch (NoSuchObjectLocalException ex) {
			LOG.error("Error while cancelling timers.", ex);
		}

	}

	@Override
	public void cancelTimerWait(long duration, AID participant) {

		for (Object obj : timerService.getTimers()) {
			Timer timer = (javax.ejb.Timer) obj;
			String[] info = timer.getInfo().toString().split(";");
			if (info.length == 4) {
				String scheduled = info[2] + info[3];
				if (scheduled.equals(duration + participant.toString())) {
					timer.cancel();
				}
			}
		}
	}

	@Timeout
	private void decide(Timer timer) {
		try {
			String[] info = timer.getInfo().toString().split(";");
			if (info[1].equals("ReplyBy")) {
				Initiator i = (Initiator) agm.getAgentReference(new AID(info[0]));
				if (i != null)
					i.ping();
			} else {
				Initiator i = (Initiator) agm.getAgentReference(new AID(info[0]));
				if (i != null)
					i.ping();
			}
		} finally {
			timer.cancel();
		}
	}
}
