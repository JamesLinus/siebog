package siebog.agents.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProps {
	private static final Logger LOG = LoggerFactory.getLogger(TestProps.class);
	private Properties props;
	private static TestProps instance;

	public static synchronized TestProps get() {
		if (instance == null)
			instance = new TestProps();
		return instance;
	}

	private TestProps() {
		props = new Properties();
		try (InputStream in = TestProps.class.getResourceAsStream("test.properties")) {
			props.load(in);
		} catch (IOException ex) {
			LOG.error("Cannot load test properties.", ex);
		}
	}

	public String getMaster() {
		return props.getProperty("host.master");
	}

	public String[] getSlaves() {
		return props.getProperty("host.slaves", "").split(",");
	}
}
