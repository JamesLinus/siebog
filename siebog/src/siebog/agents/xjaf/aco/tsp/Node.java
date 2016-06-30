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

package siebog.agents.xjaf.aco.tsp;

import java.io.Serializable;

import siebog.agents.AID;

/**
 * Represents a single graph vertex.
 *
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 */
public class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	private double x;
	private double y;
	private AID mapAgent;
	private String mapName;
	
	public Node(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Node(double x, double y, AID mapAgent) {
		this.x = x;
		this.y = y;
		this.mapAgent = mapAgent;
	}
	
	public Node(double x, double y, String mapName) {
		this.x = x;
		this.y = y;
		this.mapName = mapName;
		this.mapAgent = null;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public AID getMapAgent(){
		return mapAgent;
	}
	
	public void setMapAgent(AID mapAgent) {
		this.mapAgent = mapAgent;
	}
	
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}
	
	public String getMapName() {
		return mapName;
	}
	
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}