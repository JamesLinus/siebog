package siebog.dnars.speedtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.Statement;
import siebog.dnars.utils.importers.nt.DNarsConvert;
import siebog.dnars.utils.importers.nt.NTReader;

/**
 * A utility class which takes an NT RDF file and outputs corresponding NAL questions. A single
 * question will be produced for each NT statement, in the form of ? -> P or S -> ? (chosen
 * randomly).
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class QuestionBuilder {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("I need two parameters: InputFile OutputFile");
			return;
		}

		try (BufferedReader in = new BufferedReader(new FileReader(args[0]))) {
			try (PrintWriter out = new PrintWriter(args[1])) {
				String strLine;
				while ((strLine = in.readLine()) != null) {
					com.hp.hpl.jena.rdf.model.Statement ntStat = NTReader.str2nt(strLine);
					Statement st = DNarsConvert.toDNarsStatement(ntStat);
					Statement q;
					if (Math.random() < 0.5)
						q = new Statement(AtomicTerm.Question(), st.copula(), st.pred(), st.truth());
					else
						q = new Statement(st.subj(), st.copula(), AtomicTerm.Question(), st.truth());
					out.println(q);
				}
			}
		}
	}

}
