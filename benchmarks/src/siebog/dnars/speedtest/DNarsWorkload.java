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
	private List<String> questions;
	private int qsize;

	@Override
	public void init(Properties props) throws WorkloadException {
		String input = props.getProperty("questions");
		if (input == null || !new File(input).exists())
			throw new WorkloadException("Input file with questions (parameter 'questions') does not exist.");
		questions = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() > 0)
					questions.add(line);
			}
		} catch (IOException ex) {
			throw new WorkloadException(ex);
		}
		qsize = questions.size();
	}

	@Override
	public boolean doInsert(DB db, Object threadstate) {
		return true;
	}

	@Override
	public boolean doTransaction(DB db, Object threadstate) {
		String question = questions.get((int) (Math.random() * qsize));
		int err = db.read(null, question, null, null);
		return err == 0;
	}
}
