package siebog.dnars.inference;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//import siebog.dnars.base.AtomicTerm;
//import siebog.dnars.base.Statement;
//import siebog.dnars.graph.DNarsGraph;
//import siebog.dnars.graph.DNarsGraphFactory;
//import siebog.dnars.graph.StructuralTransform;
//import siebog.dnars.utils.importers.nt.DNarsConvert;
//import siebog.dnars.utils.importers.nt.NTReader;

// create keyspace props with replication = {'class':'SimpleStrategy', 'replication_factor':1}

//public class ResolutionEngineSpeedTest {
//	public static void main(String[] args) throws IOException {
//		if (args.length < 2) {
//			System.out.println("Arguments: DomainName SourceNtFile [AdditionalConfigMap]");
//			System.out.println("AdditionalConfigMap should be specified as a comma-separated list of key->value");
//			return;
//		}
//
//		final String domain = args[0];
//		final String ntFile = args[1];
//		final Map<String, Object> cfg = getAdditionalConfig(args);
//		DNarsGraph graph = DNarsGraphFactory.create(domain, cfg);
//		try {
//			Statement question = getRandomStatement(ntFile);
//			question = getRandomVersion(question);
//			question = setRandomQuestionmark(question);
//
//			final long start = System.currentTimeMillis();
//			Statement[] answer = ResolutionEngine.answer(graph, question, 1);
//			long total = System.currentTimeMillis() - start;
//			System.out.println("Time: " + total + " ms, answer: " + answer[0]);
//		} finally {
//			graph.shutdown();
//			System.exit(0);
//		}
//	}
//
//	private static Map<String, Object> getAdditionalConfig(String[] args) {
//		Map<String, Object> cfg = null;
//		if (args.length == 3) {
//			cfg = new HashMap<>();
//			String[] entries = args[2].split(",");
//			for (String e : entries) {
//				String[] kv = e.split("->");
//				cfg.put(kv[0], kv[1]);
//			}
//		}
//		return cfg;
//	}
//
//	private static Statement getRandomStatement(String ntFile) throws IOException {
//		String line = getRandomLine(ntFile);
//		com.hp.hpl.jena.rdf.model.Statement nt = NTReader.str2nt(line);
//		return DNarsConvert.toDNarsStatement(nt);
//	}
//
//	private static String getRandomLine(String ntFile) throws IOException {
//		Process p = Runtime.getRuntime().exec("shuf -n 1 " + ntFile);
//		try {
//			p.waitFor();
//		} catch (InterruptedException ex) {
//		}
//		try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//			return in.readLine();
//		}
//	}
//
//	private static Statement getRandomVersion(Statement st) {
//		int rnd = (int) (Math.random() * 3);
//		switch (rnd) {
//		case 0:
//			return st;
//		case 1:
//			return StructuralTransform.unpack(st).head();
//		default:
//			return StructuralTransform.unpack(st).last();
//		}
//	}
//
//	private static Statement setRandomQuestionmark(Statement st) {
//		int rnd = (int) (Math.random() * 2);
//		if (rnd == 1)
//			return new Statement(AtomicTerm.Question(), st.copula(), st.pred(), st.truth());
//		return new Statement(st.subj(), st.copula(), AtomicTerm.Question(), st.truth());
//	}
// }
