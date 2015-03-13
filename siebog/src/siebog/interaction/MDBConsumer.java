package siebog.interaction;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentManagerBean;

@MessageDriven(name = "MDBConsumer", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "topic/siebog"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MDBConsumer implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(MDBConsumer.class);
	@Inject
	private AgentManagerBean agm;

	@Override
	public void onMessage(Message msg) {
		try {
			ACLMessage acl = (ACLMessage) ((ObjectMessage) msg).getObject();
			int i = msg.getIntProperty("AIDIndex");
			AID aid = acl.receivers.get(i);
			Agent a = agm.getAgentReference(aid);
			a.handleMessage(acl);
		} catch (IllegalArgumentException ex) {
			LOG.info(ex.getMessage());
		} catch (Exception ex) {
			LOG.warn("Error while delivering a message.", ex);
		}
	}
}
