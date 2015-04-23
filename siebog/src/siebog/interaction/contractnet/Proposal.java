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

package siebog.interaction.contractnet;

import java.io.Serializable;

import siebog.agents.AID;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */
public class Proposal implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String content;
	private Serializable contentObj;
	private AID participant;
	private AID initiator;
	private boolean proposing;
	private long timeEstimate;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public AID getParticipant() {
		return participant;
	}
	public void setParticipant(AID participant) {
		this.participant = participant;
	}
	public AID getInitiator() {
		return initiator;
	}
	public void setInitiator(AID initiator) {
		this.initiator = initiator;
	}
	
	@Override
	public String toString(){
		return content;
	}
	public boolean isProposing() {
		return proposing;
	}
	public void setProposing(boolean proposing) {
		this.proposing = proposing;
	}
	public Serializable getContentObj() {
		return contentObj;
	}
	public void setContentObj(Serializable contentObj) {
		this.contentObj = contentObj;
	}
	public long getTimeEstimate() {
		return timeEstimate;
	}
	public void setTimeEstimate(long timeEstimate) {
		this.timeEstimate = timeEstimate;
	}

}
