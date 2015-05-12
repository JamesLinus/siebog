package siebog.radigost;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import siebog.starter.config.NodeConfig;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

@Singleton
@LocalBean
public class Cassandra {
	private static final String KEYSPACE = "clientagents";
	private static final String TABLE_STATES = "states";
	private Cluster cluster;
	private Session session;
	private PreparedStatement stGet;
	private PreparedStatement stSet;

	@PostConstruct
	public void postConstruct() {
		cluster = Cluster.builder().addContactPoint(NodeConfig.get().getCassandraHost()).build();
		session = cluster.connect(KEYSPACE);

		stGet = session.prepare(String.format("SELECT state FROM %s WHERE aid = ?;", TABLE_STATES));
		stSet = session.prepare(String.format("INSERT INTO %s(aid, state) VALUES(?, ?);",
				TABLE_STATES));
	}

	@PreDestroy
	public void preDestroy() {
		session.close();
		cluster.close();
	}

	public String getState(String aid) {
		BoundStatement bound = stGet.bind(aid);
		ResultSet set = session.execute(bound);
		Row row = set.one();
		return row == null ? "" : row.getString(0);
	}

	public void setState(String aid, String state) {
		BoundStatement bound = stSet.bind(aid, state);
		session.executeAsync(bound);
	}
}
