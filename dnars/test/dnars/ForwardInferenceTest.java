package dnars;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.scala.ScalaGraph;
import dnars.base.Statement;
import dnars.gremlin.DNarsGraph;
import dnars.inference.ForwardInference;

public class ForwardInferenceTest
{
	@Test
	public void deduction_analogy()
	{
		// M -> P  
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		DNarsGraph graph = DNarsGraph.wrap(ScalaGraph.wrap(new TinkerGraph()));
		try
		{
			Statement[] kb = createAndAdd(graph,
				"cat -> animal (0.82, 0.91)",
				"water -> liquid (1.0, 0.9)",
				"tiger -> cat (0.5, 0.7)",
				"developer ~ job (1.0, 0.9)",
				"feline ~ cat (0.76, 0.83)"
			);
			for (Statement st: kb)
				ForwardInference.deduction_analogy(graph, st);
			Statement[] res = {
				StatementParser.apply("tiger -> animal " + kb[0].truth().deduction(kb[2].truth())),
				StatementParser.apply("feline -> animal " + kb[0].truth().analogy(kb[4].truth(), false))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void analogy_resemblance() 
	{
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		DNarsGraph graph = DNarsGraph.wrap(ScalaGraph.wrap(new TinkerGraph()));
		try
		{
			Statement[] kb = createAndAdd(graph,
				"developer -> job (1.0, 0.9)",
				"cat ~ feline (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (1.0, 0.9)",
				"lion ~ cat (1.0, 0.9)"
			);
			for (Statement st: kb)
				ForwardInference.analogy_resemblance(graph, st);
			Statement[] res = {
				StatementParser.apply("tiger -> feline " + kb[1].truth().analogy(kb[2].truth(), true)),
				StatementParser.apply("lion ~ feline " + kb[1].truth().resemblance(kb[4].truth()))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void abduction_comparison_analogy()
	{
		// P -> M 
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		DNarsGraph graph = DNarsGraph.wrap(ScalaGraph.wrap(new TinkerGraph()));
		try
		{
			Statement[] kb = createAndAdd(graph,
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (1.0, 0.9)",
				"developer -> job (1.0, 0.9)",
				"lion -> cat (1.0, 0.9)",
				"feline ~ cat (1.0, 0.9)"
			);
			for (Statement st: kb)
				ForwardInference.abduction_comparison_analogy(graph, st);
			Statement[] res = {
				StatementParser.apply("lion -> tiger " + kb[0].truth().abduction(kb[3].truth())),
				StatementParser.apply("lion ~ tiger " + kb[0].truth().comparison(kb[3].truth())),
				StatementParser.apply("lion -> feline " + kb[0].truth().analogy(kb[4].truth(), false)),
				StatementParser.apply("tiger -> feline " + kb[0].truth().analogy(kb[4].truth(), false)),
				StatementParser.apply("tiger -> lion " + kb[3].truth().abduction(kb[0].truth())),
				StatementParser.apply("tiger ~ lion " + kb[3].truth().comparison(kb[0].truth()))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	private Statement[] createAndAdd(DNarsGraph graph, String... statements)
	{
		Statement[] st = new Statement[statements.length];
		for (int i = 0; i < statements.length; i++)
		{
			st[i] = StatementParser.apply(statements[i]);
			graph.addStatement(st[i]);
		}
		return st;
	}
	
	private void assertGraph(DNarsGraph graph, Statement[] kb, Statement[] res)
	{
		List<Statement> all = new ArrayList<>(kb.length + res.length);
		for (Statement st: kb)
			all.add(st);
		for (Statement st: res)
			all.add(st);
		
		Statement[] gr = graph.getAllStatements();
		try
		{
			assertEquals(all.size(), gr.length);
		} catch (AssertionError e)
		{
			System.out.println("Graph statements:");
			for (Statement st: gr)
				System.out.println(st);
			throw e;
		}
		
		for (Statement st: all)
		{
			boolean found = false;
			for (int i = 0; i < gr.length && !found; i++)
				if (st.equivalent(gr[i]))
					found = true;
			assertTrue("Statement " + st + " not found.", found);
		}
	}
}
