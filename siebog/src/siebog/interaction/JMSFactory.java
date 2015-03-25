package siebog.interaction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TopicSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.core.Global;
import siebog.utils.ObjectFactory;

@Singleton
@LocalBean
public class JMSFactory {
	private Logger LOG = LoggerFactory.getLogger(JMSFactory.class);
	private Connection connection;
	// private Topic topic;
	private Queue queue;

	@PostConstruct
	public void postConstruct() {
		try {
			ConnectionFactory cf = ObjectFactory.lookup(
					"java:jboss/exported/jms/RemoteConnectionFactory", ConnectionFactory.class);
			connection = cf.createConnection();
			connection.setClientID(Global.SIEBOG_MODULE);
			connection.start();
			// topic = ObjectFactory.lookup("java:jboss/exported/jms/topic/siebog", Topic.class);
			queue = ObjectFactory.lookup("java:jboss/exported/jms/queue/siebog", Queue.class);
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
			return session.createProducer(queue);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
