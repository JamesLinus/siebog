package siebog.agents.test.wordcounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.FileUtils;
import siebog.utils.LoggerUtil;

@Stateful
@Remote(Agent.class)
public class WordCounter extends XjafAgent {
	private static final long serialVersionUID = 1L;

	private HashMap<String, Integer> wordCounts;

	@Override
	protected void onInit(AgentInitArgs args) {
		wordCounts = new HashMap<>();
	}
	
	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.logMessage(msg, myAid);
		if(msg.performative == Performative.REQUEST) {
			countWords(msg);
		}
	}
	
	private void countWords(ACLMessage msg) {
		try {
			File f = FileUtils.getFile(WordCounter.class, "files/", msg.content);
			BufferedReader in = new BufferedReader(new FileReader(f));
			
			String line;
			while((line = in.readLine()) != null) {
				String[] words = line.trim().split(" ");
				for(String w : words) {
					w = removeInterpunction(w);
					if(wordCounts.get(w) != null) {
						wordCounts.put(w, wordCounts.get(w) + 1);
					} else {
						wordCounts.put(w, 1);
					}
				}
			}
			in.close();
			
			result();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String removeInterpunction(String w) {
		return w.replace(".", "").replace(",", "").replace(",", "").replace("!", "").replace("?", "").replace(";", "");
	}
	
	private void result() {
		StringBuilder sb = new StringBuilder();
		sb.append("Results from " + myAid.getStr() + ":\n");
		for(String key : wordCounts.keySet()) {
			sb.append(key);
			sb.append(": ");
			sb.append(wordCounts.get(key));
			sb.append(", ");
		}
		LoggerUtil.log(sb.toString());
	}
}
