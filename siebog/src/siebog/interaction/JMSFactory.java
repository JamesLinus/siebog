package siebog.interaction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.core.Global;
import siebog.utils.ObjectFactory;

@Singleton
@LocalBean
public class JMSFactory {
	private Logger LOG = LoggerFactory.getLogger(JMSFactory.class);
	private Connection connection;
	private Topic topic;

	@PostConstruct
	public void postConstruct() {
		try {
			ConnectionFactory cf = ObjectFactory.lookup(
					"java:jboss/exported/jms/RemoteConnectionFactory", ConnectionFactory.class);
			connection = cf.createConnection();
			connection.setClientID(Global.SIEBOG_MODULE);
			connection.start();
			topic = ObjectFactory.lookup("java:jboss/exported/jms/topic/siebog", Topic.class);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@PreDestroy
	public void preDestroy() {
		try {
			connection.close();
		} catch (JMSException ex) {
			LOG.warn("Exception while closing the JMS connection.", ex);
		}
	}

	public Session getSession() {
		try {
			return connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public MessageProducer getProducer(Session session) {
		try {
			return session.createProducer(topic);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public MessageConsumer getConsumer(Session session, AID aid) {
		try {
			String name = aid.getStr();
			String selector = "aid = '" + name + "'";
			return session.createDurableSubscriber(topic, name, selector, false);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
