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

package siebog.agents.dnars.dbpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import siebog.dnars.base.Statement;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.utils.importers.nt.DNarsConvert;
import siebog.dnars.utils.importers.nt.DNarsImporter;
import siebog.dnars.utils.importers.nt.NTReader;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Importer {

	public static void main(String[] args) throws IOException {
		doImport("abstracts.nt", "abstracts");
		// doImport("germany.nt", "germany");
		// doImport("mc2.nt", "mc2");
		// doImport("physics.nt", "physics");
		doImport("properties.nt", "properties");
		// doImport("quantum_mechanics.nt", "quantum_mechanics");
		// doImport("theoretical_physics.nt", "theoretical_physics");
		System.out.println("Done.");
		System.exit(0);
	}

	private static void doImport(String file, String domain) throws IOException {
		clear(domain);
		DNarsGraph graph = DNarsGraphFactory.create(domain, null);
		try {

			try (BufferedReader in = new BufferedReader(new InputStreamReader(Importer.class.getResourceAsStream(file)))) {
				String line;
				while ((line = in.readLine()) != null) {
					com.hp.hpl.jena.rdf.model.Statement nt = NTReader.str2nt(line);
					Statement st = DNarsConvert.toDNarsStatement(nt);
					DNarsImporter.add(graph, st);
				}
			}
			System.out.println("Imported " + domain + ".");
		} finally {
			graph.shutdown();
		}
	}

	private static void clear(String domain) {
		DNarsGraph graph = DNarsGraphFactory.create(domain, null);
		graph.shutdown();
		graph.clear();

	}
}
