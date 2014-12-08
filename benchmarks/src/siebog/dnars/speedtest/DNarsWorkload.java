package siebog.dnars.speedtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.workloads.CoreWorkload;

public class DNarsWorkload extends CoreWorkload {
	private List<String> terms;
	private List<String> relations;
	private String keyspace;

	@Override
	public void init(Properties props) throws WorkloadException {
		keyspace = props.getProperty("keyspace");
		if (keyspace == null)
			throw new WorkloadException("Please specify the 'keyspace' property.");

		String input = props.getProperty("input");
		if (input == null || !new File(input).exists())
			throw new WorkloadException("Invalid 'input' property.");

		terms = new ArrayList<>();
		relations = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			String line;
			while ((line = in.readLine()) != null) {
				char kind = line.charAt(0);
				line = line.substring(1);
				if (kind == 'T')
					terms.add(line);
				else
					relations.add(line);
			}
		} catch (IOException ex) {
			throw new WorkloadException(ex);
		}
	}

	@Override
	public boolean doInsert(DB db, Object threadstate) {
		return true;
	}

	@Override
	public boolean doTransaction(DB db, Object threadstate) {
		String question;
		if ((int) (Math.random() * 2) == 0) {
			int n = (int) (Math.random() * terms.size());
			String term = terms.get(n);
			question = term + " -> ? (1.0, 0.9)";
		} else {
			int n = (int) (Math.random() * relations.size());
			String relation = relations.get(n);
			question = "? -> " + relation + " (1.0, 0.9)";
		}

		int err = db.read(keyspace, question, null, null);
		return err == 0;
	}
}
