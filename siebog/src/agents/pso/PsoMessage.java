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

package agents.pso;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * Implementation of a message object
 * 
 * @author <a href="mailto:simic.dragan@hotmail.com">Dragan Simic</a>
 */
public class PsoMessage implements Serializable {

	private static final long serialVersionUID = 4722728917078327109L;

	public static final String UPDATE_GLOBAL_SOLUTION = "UPDATE_GLOBAL_SOLUTION";
	public static final String ITERATE_PARTICLE = "ITERATE_PARTICLE";

	private String action;
	private double fitness;
	private double[] position;

	/**
	 * @param action
	 * @param fitness
	 * @param position
	 */
	public PsoMessage(String action, double fitness, double[] position) {
		super();
		this.action = action;
		this.fitness = fitness;
		this.position = new double[position.length];
		System.arraycopy(position, 0, this.position, 0, position.length);
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @return the fitness
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * @return the position
	 */
	public double[] getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "PsoMessage [action=" + action + ", fitness=" + fitness + ", position=" + Arrays.toString(position)
				+ "]";
	}

}