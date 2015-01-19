package siebog.dnars.speedtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import scala.collection.Iterator;
import siebog.dnars.base.Statement;
import siebog.dnars.utils.importers.nt.DNarsConvert;
import siebog.dnars.utils.importers.nt.NTReader;

/**
 * A utility class which takes an NT RDF file, and outputs individual properties, i.e. subjects,
 * predicates, and objects.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class PropsExtractor {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("I need two parameters: InputNtFile OutputPropsFile");
			return;
		}

		int n = 0, numErrors = 0;
		Set<String> props = new HashSet<>();
		try (BufferedReader in = new BufferedReader(new FileReader(args[0]))) {
			String strLine;
			while ((strLine = in.readLine()) != null) {
				if (strLine.startsWith("#"))
					continue;
				try {
					com.hp.hpl.jena.rdf.model.Statement ntStat = NTReader.str2nt(strLine);
					Statement st = DNarsConvert.toDNarsStatement(ntStat);
					Iterator<Statement> i = st.allForms().iterator();
					// (x A B) -> C :: write C
					props.add("R" + i.next().pred());
					// A -> (/ C * B) :: write A
					props.add("T" + i.next().subj());
					// B -> (/ C A * B) :: write B
					props.add("T" + i.next().subj());
				} catch (com.hp.hpl.jena.shared.SyntaxError ex) {
					++numErrors;
				}

				++n;
				if (n % 500000 == 0)
					System.out.println("Completed " + n);
			}
		}

		System.out.println("Data read, " + numErrors + " errors.");
		System.out.println("Saving to " + args[1]);
		try (PrintWriter out = new PrintWriter(args[1])) {
			for (String str : props)
				out.println(str);
		}
	}
}
