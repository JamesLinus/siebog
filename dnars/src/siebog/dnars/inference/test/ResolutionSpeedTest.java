package siebog.dnars.inference.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.inference.ResolutionEngine;
import com.tinkerpop.blueprints.Vertex;

public class ResolutionSpeedTest {
	private static class Worker extends Thread {
		private long iterations;
		private long totalTime;
		private DNarsGraph graph;
		private ResolutionEngine resolution;

		public Worker(DNarsGraph graph) {
			this.graph = graph;
			resolution = new ResolutionEngine(graph);
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				Statement q = getQuestion();
				long start = System.currentTimeMillis();
				resolution.answer(q);
				long time = System.currentTimeMillis() - start;
				++iterations;
				totalTime += time;
				/*
				 * try { Thread.sleep((int) (Math.random() * 100) + 100); } catch (InterruptedException e) { break; }
				 */
			}
		}

		private Statement getQuestion() {
			Vertex v = graph.getRandomVertex();
			String term = (String) v.getProperty("term");

			String str;
			if (term.startsWith("(x"))
				str = term + " -> ?";
			else if (term.startsWith("(/"))
				str = "? -> " + term;
			else if (term.startsWith("(\\"))
				str = term + " -> ?";
			else if (Math.random() < 0.5)
				str = "? -> " + term;
			else
				str = term + " -> ?";
			return StatementParser.apply(str);
		}

		public long getIterations() {
			return iterations;
		}

		public long getTotalTime() {
			return totalTime;
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		if (args.length != 2) {
			System.out.println("I need two arguments: Domain NumberOfThreads");
			return;
		}
		final String domain = args[0];
		final int numThreads = Integer.parseInt(args[1]);

		DNarsGraph graph = DNarsGraphFactory.create(domain, null);
		try {
			while (true) {
				List<Worker> workers = new ArrayList<>(numThreads);
				for (int i = 0; i < numThreads; i++) {
					Worker w = new Worker(graph);
					workers.add(w);
					w.start();
				}
				Thread.sleep(30000);
				long iterations = 0, totalTime = 0;
				for (Worker w : workers) {
					w.interrupt();
					w.join();
					iterations += w.getIterations();
					totalTime += w.getTotalTime();
				}
				System.out.println(totalTime + ":" + iterations + ":" + (totalTime / iterations));
			}
		} finally {
			graph.shutdown();
		}

	}
}
