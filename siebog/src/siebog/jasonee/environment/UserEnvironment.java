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
 * 
 * Based on the implementation of jason.environment.Environment.
 * Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 * 
 * To contact the original authors:
 * http://www.inf.ufrgs.br/~bordini
 * http://www.das.ufsc.br/~jomi
 */

package siebog.jasonee.environment;

import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class UserEnvironment {
	private static Logger logger = Logger.getLogger(Environment.class.getName());
	private List<Literal> percepts = Collections.synchronizedList(new ArrayList<Literal>());
	private Map<String, List<Literal>> agPercepts = new ConcurrentHashMap<String, List<Literal>>();
	private boolean isRunning = true;
	// set of agents that already received the last version of perception
	private Set<String> uptodateAgs = Collections.synchronizedSet(new HashSet<String>());
	private String envName;

	/**
	 * Called before the MAS execution with the args informed in .mas2j project, the user environment could override it.
	 */
	public void init(String envName, String[] args) {
		this.envName = envName;
	}

	/**
	 * Called just before the end of MAS execution, the user environment could override it.
	 */
	public void stop() {
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * @deprecated use version with String... parameter
	 */
	public void informAgsEnvironmentChanged(Collection<String> agents) {
		informAgsEnvironmentChanged(agents.toArray(new String[0]));
	}

	public void informAgsEnvironmentChanged(String... agents) {
		getEnv().informAgsEnvironmentChanged(agents);
	}

	/**
	 * Returns percepts for an agent. A full copy of both common and agent's percepts lists is returned.
	 * 
	 * It returns null if the agent's perception doesn't changed since last call.
	 * 
	 * This method is to be called by TS and should not be called by other objects.
	 */
	public List<Literal> getPercepts(String agName) { // TODO in a future release, call this method doPerception, and
														// get simply returns the list

		// check whether this agent needs the current version of perception
		if (uptodateAgs.contains(agName)) {
			return null;
		}
		// add agName in the set of updated agents
		uptodateAgs.add(agName);

		int size = percepts.size();
		List<Literal> agl = agPercepts.get(agName);
		if (agl != null) {
			size += agl.size();
		}
		List<Literal> p = new ArrayList<Literal>(size);

		if (!percepts.isEmpty()) { // has global perception?
			synchronized (percepts) {
				// make a local copy of the environment percepts
				// Note: a deep copy will be done by BB.add
				p.addAll(percepts);
			}
		}
		if (agl != null) { // add agent personal perception
			synchronized (agl) {
				p.addAll(agl);
			}
		}

		return p;
	}

	/**
	 * Returns a copy of the perception for an agent.
	 * 
	 * It is the same list returned by getPercepts, but doesn't consider the last call of the method.
	 */
	public List<Literal> consultPercepts(String agName) {
		int size = percepts.size();
		List<Literal> agl = agPercepts.get(agName);
		if (agl != null) {
			size += agl.size();
		}
		List<Literal> p = new ArrayList<Literal>(size);

		if (!percepts.isEmpty()) { // has global perception?
			synchronized (percepts) {
				// make a local copy of the environment percepts
				// Note: a deep copy will be done by BB.add
				p.addAll(percepts);
			}
		}
		if (agl != null) { // add agent personal perception
			synchronized (agl) {
				p.addAll(agl);
			}
		}
		return p;
	}

	/** Adds a perception for all agents */
	public void addPercept(Literal per) {
		if (per != null) {
			if (!percepts.contains(per)) {
				percepts.add(per);
				uptodateAgs.clear();
			}
		}
	}

	/** Removes a perception from the common perception list */
	public boolean removePercept(Literal per) {
		if (per != null) {
			uptodateAgs.clear();
			return percepts.remove(per);
		}
		return false;
	}

	/**
	 * Removes all percepts from the common perception list that unifies with <i>per</i>.
	 * 
	 * Example: removePerceptsByUnif(Literal.parseLiteral("position(_)")) will remove all percepts that unifies
	 * "position(_)".
	 * 
	 * @return the number of removed percepts.
	 */
	public int removePerceptsByUnif(Literal per) {
		int c = 0;
		if (!percepts.isEmpty()) { // has global perception?
			synchronized (percepts) {
				Iterator<Literal> i = percepts.iterator();
				while (i.hasNext()) {
					Literal l = i.next();
					if (new Unifier().unifies(l, per)) {
						i.remove();
						c++;
					}
				}
			}
			if (c > 0)
				uptodateAgs.clear();
		}
		return c;
	}

	/** Clears the list of global percepts */
	public void clearPercepts() {
		if (!percepts.isEmpty()) {
			uptodateAgs.clear();
			percepts.clear();
		}
	}

	/** Returns true if the list of common percepts contains the perception <i>per</i>. */
	public boolean containsPercept(Literal per) {
		if (per != null) {
			return percepts.contains(per);
		}
		return false;
	}

	/** Adds a perception for a specific agent */
	public void addPercept(String agName, Literal... per) {
		if (per != null && agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl == null) {
				agl = Collections.synchronizedList(new ArrayList<Literal>());
				agPercepts.put(agName, agl);
			}
			for (Literal p : per) {
				if (!agl.contains(p)) {
					uptodateAgs.remove(agName);
					agl.add(p);
				}
			}
		}
	}

	/** Removes a perception for an agent */
	public boolean removePercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl != null) {
				uptodateAgs.remove(agName);
				return agl.remove(per);
			}
		}
		return false;
	}

	/**
	 * Removes from an agent perception all percepts that unifies with <i>per</i>.
	 * 
	 * @return the number of removed percepts.
	 */
	public int removePerceptsByUnif(String agName, Literal per) {
		int c = 0;
		if (per != null && agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl != null) {
				synchronized (agl) {
					Iterator<Literal> i = agl.iterator();
					while (i.hasNext()) {
						Literal l = i.next();
						if (new Unifier().unifies(l, per)) {
							i.remove();
							c++;
						}
					}
				}
				if (c > 0)
					uptodateAgs.remove(agName);
			}
		}
		return c;
	}

	public boolean containsPercept(String agName, Literal per) {
		if (per != null && agName != null) {
			@SuppressWarnings("rawtypes")
			List agl = (List) agPercepts.get(agName);
			if (agl != null) {
				return agl.contains(per);
			}
		}
		return false;
	}

	/** Clears the list of percepts of a specific agent */
	public void clearPercepts(String agName) {
		if (agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl != null) {
				uptodateAgs.remove(agName);
				agl.clear();
			}
		}
	}

	/** Clears all perception (from common list and individual perceptions) */
	public void clearAllPercepts() {
		clearPercepts();
		for (String ag : agPercepts.keySet())
			clearPercepts(ag);
	}

	/**
	 * Called by the agent infrastructure to schedule an action to be executed on the environment
	 */
	public void scheduleAction(final String agName, final Structure action, final Object infraData) {
		ObjectFactory.getExecutorService().execute(new Runnable() {
			public void run() {
				if (isRunning)
					try {
						boolean success = executeAction(agName, action);
						// send the result of the execution to the agent
						getEnv().actionExecuted(agName, action, success, infraData);
					} catch (Exception ie) {
						if (!(ie instanceof InterruptedException)) {
							logger.log(Level.WARNING, "act error!", ie);
						}
					}
			}
		});
	}

	/**
	 * Executes an action on the environment. This method is probably overridden in the user environment class.
	 */
	public boolean executeAction(String agName, Structure act) {
		logger.info("The action " + act + " done by " + agName + " is not implemented in the default environment.");
		return false;
	}

	private Environment getEnv() {
		return ObjectFactory.getJasonEEApp().getEnv(envName);
	}

}
