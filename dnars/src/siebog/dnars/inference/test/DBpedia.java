/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package siebog.dnars.inference.test;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import siebog.dnars.base.Statement;
//import siebog.dnars.base.StatementParser;
//import siebog.dnars.graph.DNarsGraph;
//import siebog.dnars.graph.DNarsGraphFactory;
//import siebog.dnars.inference.ForwardInference;
//import siebog.dnars.inference.ResolutionEngine;
//import siebog.dnars.utils.importers.nt.DNarsConvert;
//import siebog.dnars.utils.importers.nt.NTReader;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
//public class DBpedia {
// private static final String COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";
//	private Map<String, DNarsGraph> domains;
//
//	public DBpedia(String[] domains) {
//		this.domains = new HashMap<>();
//		for (String str : domains)
//			this.domains.put(str, DNarsGraphFactory.create(str, null));
//	}
//
//	public List<Statement> getAnswers(String term) {
//		List<Statement> answers = new ArrayList<>();
//		final Collection<DNarsGraph> domains = this.domains.values();
//		getExtensions(term, domains, answers);
//		getIntensions(term, domains, answers);
//		return answers;
//	}
//
//	public List<Statement> getConclusions(Statement input) {
//		DNarsGraph graph = domains.get("properties");
//		return null;
//	}
//
//	private void getExtensions(String term, Collection<DNarsGraph> domains, List<Statement> answers) {
//		Statement q = StatementParser.apply("? -> " + term);
//		getAnswers(q, domains, answers);
//	}
//
//	private void getIntensions(String term, Collection<DNarsGraph> domains, List<Statement> answers) {
//		Statement q = StatementParser.apply(term + " -> ?");
//		getAnswers(q, domains, answers);
//	}
//
//	private void getAnswers(Statement question, Collection<DNarsGraph> domains, List<Statement> answers) {
//		for (DNarsGraph graph : domains) {
//			Statement[] a = ResolutionEngine.answer(graph, question, Integer.MAX_VALUE);
//			answers.addAll(Arrays.asList(a));
//		}
//	}
//
//	public static void main(String[] args) throws IOException {
//		DBpedia db = new DBpedia(new String[] { "properties", "abstracts" });
//
//		String term = "http://dbpedia.org/resource/Albert_Einstein";
//		List<Statement> answers = db.getAnswers(term);
//		for (Statement st : answers)
//			System.out.println(st);
//
//		String[] files = { "germany.nt", "mc2.nt", "physics.nt", "quantum_mechanics.nt", "theoretical_physics.nt" };
//		for (String f : files)
//			try (BufferedReader in = new BufferedReader(new InputStreamReader(DBpedia.class.getResourceAsStream(f)))) {
//				String line;
//				while ((line = in.readLine()) != null) {
//					com.hp.hpl.jena.rdf.model.Statement nt = NTReader.str2nt(line);
//					Statement st = DNarsConvert.toDNarsStatement(nt);
//
//				}
//			}
//
//		System.exit(0);
//	}
// }
