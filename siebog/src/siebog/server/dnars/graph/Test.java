package siebog.server.dnars.graph;

import siebog.server.dnars.inference.Resolution;
import siebog.server.dnars.base.Statement;
import siebog.server.dnars.base.StatementParser;

public class Test
{
	public static void main(String[] args)
	{
		DNarsGraph g = DNarsGraphFactory.create("test2", null);
		try
		{
			Statement question = StatementParser.apply("? -> (/ http://dbpedia.org/ontology/birthPlace http://dbpedia.org/resource/Andre_Agassi *)");
			System.out.println( Resolution.answer(g, question) );
		} finally
		{
			g.shutdown(false);
		}
	}
}
