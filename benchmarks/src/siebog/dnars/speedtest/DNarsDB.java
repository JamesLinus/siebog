package siebog.dnars.speedtest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.inference.ResolutionEngine;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;

public class DNarsDB extends DB {
	private Map<String, Object> config;

	@Override
	public void init() throws DBException {
		Properties props = getProperties();
		config = new HashMap<>();
		String hostname = props.getProperty("hostname");
		if (hostname == null)
			throw new DBException("Database hostname cannot be empty.");
		config.put("storage.hostname", hostname);
	}

	@Override
	public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
		if ((int) (Math.random() * 2) == 0)
			key = key + " -> ? (1.0, 0.9)";
		else
			key = "? -> " + key + " (1.0, 0.9)";
		Statement question = StatementParser.apply(key);
		System.out.println("Reading " + question);

		int err = 0;
		DNarsGraph graph = DNarsGraphFactory.create(table, config);
		try {
			Statement[] answers = ResolutionEngine.answer(graph, question, Integer.MAX_VALUE);
			if (answers.length == 0)
				err = 1;
		} finally {
			graph.shutdown();
		}

		return err;
	}

	@Override
	public int delete(String table, String key) {
		return 0;
	}

	@Override
	public int insert(String table, String key, HashMap<String, ByteIterator> values) {
		return 0;
	}

	@Override
	public int scan(String table, String startkey, int recordcount, Set<String> fields,
			Vector<HashMap<String, ByteIterator>> result) {
		return 0;
	}

	@Override
	public int update(String table, String key, HashMap<String, ByteIterator> values) {
		return 0;
	}

	public static void main(String[] args) {
	}
}
