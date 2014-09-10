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

package siebog.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ColumnTree;
import com.smartgwt.client.widgets.grid.events.NodeSelectedEvent;
import com.smartgwt.client.widgets.grid.events.NodeSelectedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Admin implements EntryPoint {
	private ColumnTree agents;
	private MessagingForm msgForm;

	@Override
	public void onModuleLoad() {
		agents = new ColumnTree();
		agents.setWidth100();
		agents.setHeight100();
		agents.setShowHeaders(true);
		agents.setShowNodeCount(true);
		agents.setLoadDataOnDemand(false);
		agents.addNodeSelectedHandler(new NodeSelectedHandler() {
			@Override
			public void onNodeSelected(NodeSelectedEvent event) {
				updateControls();
			}
		});

		msgForm = new MessagingForm();
		msgForm.setWidth100();

		// startGuiAgent();
		updateControls();

		SectionStackSection agentsSection = new SectionStackSection();
		agentsSection.setTitle("Available XJAF agents");
		agentsSection.setExpanded(true);
		agentsSection.setItems(new AgentView(this));

		SectionStackSection jasonSection = new SectionStackSection();
		jasonSection.setTitle("Load mas2j project");
		jasonSection.setExpanded(true);
		jasonSection.setItems();

		SectionStackSection msgSection = new SectionStackSection();
		msgSection.setTitle("Messages");
		msgSection.setExpanded(true);
		msgSection.setItems(msgForm);

		SectionStack stack = new SectionStack();
		stack.setWidth100();
		stack.setVisibilityMode(VisibilityMode.MULTIPLE);
		stack.setAnimateSections(true);
		stack.setOverflow(Overflow.HIDDEN);
		stack.setSections(agentsSection, jasonSection, msgSection);

		ToolStrip toolStrip = new ToolStrip();
		Label title = new Label("<h2>The Siebog Multiagent Framework</h2>");
		title.setWrap(false);
		toolStrip.setAlign(Alignment.CENTER);
		toolStrip.addMember(title);

		VLayout main = new VLayout();
		main.setWidth(800);
		LayoutSpacer space = new LayoutSpacer();
		space.setHeight(32);
		main.addMembers(toolStrip, space, stack);

		HLayout root = new HLayout();
		root.setWidth100();
		root.setHeight(700);
		root.setAlign(Alignment.CENTER);
		root.addMember(main);
		root.draw();
	}

	public void onRunningAgents(JsArray<RunningAgentWrapper> running) {
		msgForm.onRunningList(running);
	}

	private void updateControls() {
		// final ListGridRecord[] rec = agents.getSelection(COLUMN_CLASSES);
		// start.setDisabled(rec == null || rec.length != 1);
	}

	/*
	 * private void startGuiAgent() { startNewAgent("siebog$GUIAgent", "GUIAgent"); final Timer timer = new Timer() {
	 * 
	 * @Override public void run() { RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/guiagent");
	 * builder.setHeader("Content-Type", "application/json"); try { builder.sendRequest("", new RequestCallback() {
	 * 
	 * @Override public void onResponseReceived(Request req, Response resp) { final String str = resp.getText(); if (str
	 * != null && str.length() > 0) SC.say("Message received", str); }
	 * 
	 * @Override public void onError(Request req, Throwable ex) { ex.printStackTrace(); } }); } catch (RequestException
	 * ex) { ex.printStackTrace(); } } }; timer.scheduleRepeating(1000); }
	 */
}
