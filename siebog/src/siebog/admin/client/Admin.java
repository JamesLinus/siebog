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
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.grid.ColumnTree;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Admin implements EntryPoint {
	private ColumnTree agents;
	private IButton reloadAgents;
	private IButton start;
	private MessagingForm msgForm;

	@Override
	public void onModuleLoad() {
		agents = new ColumnTree();
		agents.setWidth("80%");
		agents.setHeight100();
		agents.setShowHeaders(true);
		agents.setShowNodeCount(true);
		agents.setLoadDataOnDemand(false);

		msgForm = new MessagingForm();
		msgForm.setWidth("20%");

		HLayout main = new HLayout();
		main.setWidth(800);
		main.setHeight100();
		main.setMembersMargin(8);
		main.addMembers(agents, msgForm);

		reloadAgents();
		// startGuiAgent();
		reloadRunning();
		updateControls();

		HLayout root = new HLayout();
		root.setWidth100();
		root.setHeight(600);
		root.setAlign(Alignment.CENTER);
		root.addMember(main);
		root.draw();
	}

	private void makeClasses() {
		/*
		 * reloadAgents = new IButton("Reload", new ClickHandler() {
		 * 
		 * @Override public void onClick(ClickEvent event) { reloadAgents(); } }); start = new IButton("Start", new
		 * ClickHandler() {
		 * 
		 * @Override public void onClick(ClickEvent event) { final ListGridRecord rec = classes.getSelectedRecord(); if
		 * (rec != null) SC.askforValue("Start new agent", "Runtime name for the new agent?", new ValueCallback() {
		 * 
		 * @Override public void execute(String value) { if (value != null) { String agClass =
		 * rec.getAttribute("module") + '$' + rec.getAttribute("ejbName"); startNewAgent(agClass, value); } } }); } });
		 * 
		 * controls.addMembers(reloadAgents, start);
		 */

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
			node.setIcon("../img/agent.png");
			nodes.add(node);
		}
		return nodes.toArray(new TreeNode[0]);
	}

	private void startNewAgent(String agClass, String name) {
		try {
			String url = "rest/agents/running/" + agClass + "/" + name;
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
		/*
		 * RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/agents/running");
		 * builder.setHeader("Content-Type", "application/json"); try { builder.sendRequest("", new RequestCallback() {
		 * 
		 * @Override public void onResponseReceived(Request req, Response resp) { JsArray<AIDWrapper> result =
		 * JsonUtils.unsafeEval(resp.getText()); final int n = result.length(); ListGridRecord[] rec = new
		 * ListGridRecord[n]; for (int i = 0; i < n; i++) rec[i] = new ListGridRecord(result.get(i));
		 * running.setData(rec); msgForm.onRunningList(result); updateControls(); }
		 * 
		 * @Override public void onError(Request req, Throwable ex) { updateControls(); ex.printStackTrace(); } }); }
		 * catch (RequestException ex) { ex.printStackTrace(); }
		 */
	}

	private void updateControls() {
		// start.setDisabled(agents.getSelectedRecord() == null);
	}

	private void startGuiAgent() {
		startNewAgent("siebog$GUIAgent", "GUIAgent");
		final Timer timer = new Timer() {
			@Override
			public void run() {
				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/guiagent");
				builder.setHeader("Content-Type", "application/json");
				try {
					builder.sendRequest("", new RequestCallback() {
						@Override
						public void onResponseReceived(Request req, Response resp) {
							final String str = resp.getText();
							if (str != null && str.length() > 0)
								SC.say("Message received", str);
						}

						@Override
						public void onError(Request req, Throwable ex) {
							ex.printStackTrace();
						}
					});
				} catch (RequestException ex) {
					ex.printStackTrace();
				}
			}
		};
		timer.scheduleRepeating(1000);
	}
}
