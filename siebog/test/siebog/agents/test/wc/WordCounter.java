package siebog.agents.test.wc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

@Stateful
@Remote(Agent.class)
@Clustered
public class WordCounter extends XjafAgent {
	private static final Logger LOG = LoggerFactory.getLogger(WordCounter.class);
	private static final long serialVersionUID = 1L;
	private ArrayList<String> lines;
	private ArrayList<Integer> wordCounts;
	private int iii;

	@Override
	protected void onMessage(ACLMessage msg) {
		// LOG.info("Agent {} processing lines at {}.", myAid.getName(), getNodeName());
		// if (lines == null) {
		// lines = readLines(msg.content);
		// wordCounts = new ArrayList<Integer>(lines.size());
		// }
		LOG.info("onMessage of {}", myAid.getName());
		processLine();
	}

	private void processLine() {
		// int processed = wordCounts.size();
		// if (processed < lines.size()) {
		// int wc = getWordCount(lines.get(processed));
		// wordCounts.add(wc);
		if (++iii < 3)
			doNextLine();
		// } else {
		// LOG.info("Agent {} counter: {}", myAid.getName(), wordCounts);
		// }
	}

	private int getWordCount(String line) {
		return line.split("\\s+").length;
	}

	private void doNextLine() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			return;
		}
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.receivers.add(myAid);
		msm().post(msg);
	}

	private ArrayList<String> readLines(String fileName) {
		ArrayList<String> lines = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = in.readLine()) != null)
				lines.add(line);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
		return lines;
	}

}
