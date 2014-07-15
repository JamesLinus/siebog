package dnars.gremlin;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.scala.ScalaGraph;
import dnars.StatementParser;
import dnars.base.Statement;
import dnars.base.Truth;

public class DNarsGraphTest
{
	@Test
	public void test()
	{
		DNarsGraph graph = DNarsGraph.wrap(ScalaGraph.wrap(new TinkerGraph()));
		try
		{		
			Statement s1 = StatementParser.apply("cat -> animal (0.8, 0.77)");
			Statement s2 = StatementParser.apply("cat -> mammal (1.0, 0.9)");
			Statement s3 = StatementParser.apply("tiger -> cat (1.0, 0.9)");
			
			graph.addStatement(s1);
			graph.addStatement(s2);
			graph.addStatement(s3);
			
			graph.assertStatement(s1);
			graph.assertStatement(s2);
			graph.assertStatement(s3);
			
			// test if revision is correctly applied when the statement exists
			graph.addStatement(s1);
			try
			{
				graph.assertStatement(s1);
			} catch (IllegalArgumentException ex)
			{
				Truth revised = s1.truth().revision(s1.truth());
				assertEquals(revised.toString(), ex.getMessage());
			}
		} finally
		{
			graph.shutdown();
		}
	}
}
