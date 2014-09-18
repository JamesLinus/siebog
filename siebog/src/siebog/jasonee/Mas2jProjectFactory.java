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

package siebog.jasonee;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Mas2jProjectFactory {
	public static MAS2JProject load(File mas2jFile) {
		try (InputStream in = new FileInputStream(mas2jFile)) {
			mas2j parser = new mas2j(in);
			MAS2JProject project = parser.mas();

			project.setupDefault();
			project.registerDirectives();
			((Include) DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());

			fixAgentSrc(project, mas2jFile.getParent());

			return project;
		} catch (IOException | ParseException ex) {
			throw new IllegalArgumentException("Error while loading " + mas2jFile, ex);
		}
	}

	private static void fixAgentSrc(MAS2JProject project, String projectRoot) {
		for (AgentParameters agp : project.getAgents()) {
			final String fileName = agp.asSource.getName();
			for (String path : project.getSourcePaths()) {
				File f = new File(projectRoot, path + "/" + fileName);
				if (f.exists()) {
					agp.asSource = f;
					break;
				}
			}
		}
	}
}
