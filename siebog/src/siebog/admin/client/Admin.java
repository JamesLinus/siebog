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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ColumnTree;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Admin implements EntryPoint {
	private static final int COLUMN_CLASSES = 1;
	private ColumnTree agents;
	private IButton reloadAgents;
	private IButton start;
	private MessagingForm msgForm;

	@Override
	public void onModuleLoad() {
		agents = new ColumnTree();
		agents.setWidth100();
		agents.setHeight100();
		agents.setShowHeaders(true);
		agents.setShowNodeCount(true);
		agents.setLoadDataOnDemand(false);

		msgForm = new MessagingForm();
		msgForm.setWidth100();

		reloadAgents = new IButton("Reload", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reloadAgents();
			}
		});
		start = new IButton("Start", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord[] rec = agents.getSelection(COLUMN_CLASSES);
				if (rec != null && rec.length == 1) {
					SC.askforValue("Start new agent", "Runtime name for the new agent?", new ValueCallback() {
						@Override
						public void execute(String value) {
							if (value != null && value.length() > 0) {
								AgentClassWrapper agClass = (AgentClassWrapper) rec[0]
										.getAttributeAsJavaScriptObject("agClass");
								startNewAgent(agClass, value);
							}
						}
					});
				}
			}
		});

		reloadAgents();
		// startGuiAgent();
		reloadRunning();
		updateControls();

		SectionStackSection agentsSection = new SectionStackSection();
		agentsSection.setTitle("Available modules and agent classes");
		agentsSection.setExpanded(true);
		HLayout h = new HLayout();
		h.setWidth100();
		h.setMembersMargin(4);
		h.addMembers(reloadAgents, start);
		agentsSection.setItems(h, agents);

		SectionStackSection msgSection = new SectionStackSection();
		msgSection.setTitle("Messages");
		msgSection.setExpanded(true);
		msgSection.setItems(msgForm);

		SectionStack stack = new SectionStack();
		stack.setWidth100();
		stack.setVisibilityMode(VisibilityMode.MULTIPLE);
		stack.setAnimateSections(true);
		stack.setOverflow(Overflow.HIDDEN);
		stack.setSections(agentsSection, msgSection);

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

	private void reloadAgents() {
		RequestBuilderUtil.get("rest/agents/classes", new RequestCallback() {
			@Override
			public void onResponseReceived(Request req, Response resp) {
				Tree tree = new Tree();
				tree.setModelType(TreeModelType.PARENT);
				tree.setIdField("id");
				tree.setParentIdField("parent");
				tree.setNameProperty("name");
				final String ROOT_ID = "1";
				tree.setRootValue(ROOT_ID);

				JsArray<AgentClassWrapper> result = JsonUtils.unsafeEval(resp.getText());
				tree.setData(getAgentNodes(result, ROOT_ID));
				agents.setData(tree);
				updateControls();
			}

			@Override
			public void onError(Request req, Throwable ex) {
				updateControls();
				ex.printStackTrace();
			}
		});
	}

	private TreeNode[] getAgentNodes(JsArray<AgentClassWrapper> array, String rootId) {
		final int n = array.length();
		Set<String> addedModules = new HashSet<>();
		List<TreeNode> nodes = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			final AgentClassWrapper agClass = array.get(i);
			final String module = agClass.getModule();
			final String ejbName = agClass.getEjbName();
			if (addedModules.add(module)) {
				TreeNode node = new AgentTreeNode(module, rootId, module);
				node.setIcon("../img/module.png");
				nodes.add(node);
			}
			TreeNode node = new AgentTreeNode(module + ejbName, module, ejbName);
			node.setAttribute("agClass", agClass);
			node.setIcon("../img/agent.png");
			nodes.add(node);
		}
		return nodes.toArray(new TreeNode[0]);
	}

	private void startNewAgent(AgentClassWrapper agClass, String name) {
		try {
			String url = "rest/agents/running/" + agClass.getFullId() + "/" + name;
			RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
			builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
			builder.sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					reloadRunning();
					updateControls();
				}

				@Override
				public void onError(Request req, Throwable ex) {
					ex.printStackTrace();
					updateControls();
				}
			});
		} catch (RequestException ex) {
			ex.printStackTrace();
		} finally {
			updateControls();
		}
	}

	private void reloadRunning() {
		RequestBuilderUtil.get("rest/agents/running", new RequestCallback() {

			@Override
			public void onResponseReceived(Request req, Response resp) {
				JsArray<AIDWrapper> result = JsonUtils.unsafeEval(resp.getText());
				final int n = result.length();
				ListGridRecord[] rec = new ListGridRecord[n];
				for (int i = 0; i < n; i++)
					rec[i] = new ListGridRecord(result.get(i));
				// running.setData(rec);
				msgForm.onRunningList(result);
				updateControls();
			}

			@Override
			public void onError(Request req, Throwable ex) {
				ex.printStackTrace();
			}
		});
	}

	private void updateControls() {
		final ListGridRecord[] rec = agents.getSelection(COLUMN_CLASSES);
		start.setDisabled(rec == null || rec.length != 1);
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
