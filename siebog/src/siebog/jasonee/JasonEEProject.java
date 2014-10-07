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

import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import siebog.core.FileUtils;

/**
 * Description of a Jason EE project that can be sent to remote nodes. Includes the full source of mas2j and of all the
 * referenced agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonEEProject implements Serializable {
	private static final long serialVersionUID = 1L;
	private String mas2jSource;
	private transient MAS2JProject mas2j;
	// name -> source
	private Map<String, String> agentSources;

	public JasonEEProject() {
		agentSources = new HashMap<>();
	}

	public static JasonEEProject loadFromFile(File file) {
		try {
			JasonEEProject p = new JasonEEProject();
			p.mas2jSource = FileUtils.read(file);

			MAS2JProject mas2j = p.getMas2j();
			for (AgentParameters agp : mas2j.getAgents()) {
				final String fileName = agp.asSource.getName();
				for (String path : mas2j.getSourcePaths()) {
					File f = new File(file.getParent(), path + "/" + fileName);
					if (f.exists()) {
						p.agentSources.put(agp.name, FileUtils.read(f));
						break;
					}
				}
			}

			return p;
		} catch (IllegalStateException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new IllegalArgumentException("There was a problem with reading the file " + file, ex);
		}
	}

	public String getMas2jSource() {
		return mas2jSource;
	}

	public void setMas2jSource(String mas2jSource) {
		this.mas2jSource = mas2jSource;
	}

	public Map<String, String> getAgentSources() {
		return agentSources;
	}

	public void setAgentSources(Map<String, String> agentSources) {
		this.agentSources = agentSources;
	}

	public String getAgentSource(String name) {
		return agentSources.get(name);
	}

	public MAS2JProject getMas2j() {
		try {
			if (mas2j == null) {
				jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new StringReader(mas2jSource));
				mas2j = parser.mas();

				mas2j.setupDefault();
				mas2j.registerDirectives();
				((Include) DirectiveProcessor.getDirective("include")).setSourcePath(mas2j.getSourcePaths());
			}
			return mas2j;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Cannot parse mas2j source.", ex);
		}
	}
}
