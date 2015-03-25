package siebog.interaction;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentManagerBean;

@MessageDriven(name = "MDBConsumer", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/siebog"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MDBConsumer implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(MDBConsumer.class);
	@Inject
	private AgentManagerBean agm;

	@Override
	public void onMessage(Message msg) {
		try {
			processMessage(msg);
		} catch (JMSException ex) {
			LOG.warn("Cannot process an incoming message.", ex);
		}
	}

	private void processMessage(Message msg) throws JMSException {
		ACLMessage acl = (ACLMessage) ((ObjectMessage) msg).getObject();
		AID aid = getAid(msg, acl);
		deliverMessage(acl, aid);
	}

	private AID getAid(Message msg, ACLMessage acl) throws JMSException {
		int i = msg.getIntProperty("AIDIndex");
		return acl.receivers.get(i);
	}

	private void deliverMessage(ACLMessage msg, AID aid) {
		Agent agent = agm.getAgentReference(aid);
		if (agent != null) {
			agent.handleMessage(msg);
		} else {
			LOG.info("No such agent: {}", aid.getName());
		}
	}
}
