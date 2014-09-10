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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionUpdatedEvent;
import com.smartgwt.client.widgets.grid.events.SelectionUpdatedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AgentView extends HLayout {
	private Admin admin;
	private Map<String, List<String>> module2agents;
	private ListGrid modules;
	private ListGrid classes;
	private HLayout rolloverCanvas;
	private ListGridRecord rolloverRecord;

	public AgentView(Admin admin) {
		this.admin = admin;
		module2agents = new HashMap<>();

		setWidth100();
		setHeight100();

		modules = new ListGrid();
		modules.setWidth("50%");
		modules.setHeight100();
		modules.setFields(new ListGridField("name", "Module"));
		modules.addSelectionUpdatedHandler(new SelectionUpdatedHandler() {
			@Override
			public void onSelectionUpdated(SelectionUpdatedEvent event) {
				reloadClassesForSelectedModule();
			}
		});

		classes = new ListGrid() {
			@Override
			protected Canvas getRollOverCanvas(Integer rowNum, Integer colNum) {
				rolloverRecord = getRecord(rowNum);
				if (rolloverCanvas == null)
					buildRolloverCanvas();
				return rolloverCanvas;
			}
		};
		classes.setWidth("50%");
		classes.setHeight100();
		classes.setShowRollOverCanvas(true);
		classes.setFields(new ListGridField("name", "Agent Class"));

		addMembers(modules, classes);

		reloadAgentClasses();
	}

	private void buildRolloverCanvas() {
		rolloverCanvas = new HLayout();
		rolloverCanvas.setSnapTo("TR");
		rolloverCanvas.setWidth(20);
		rolloverCanvas.setHeight(20);

		ImgButton btn = new ImgButton();
		btn.setSrc("../img/start.png");
		btn.setShowDown(false);
		btn.setShowRollOver(false);
		btn.setLayoutAlign(Alignment.CENTER);
		btn.setWidth(16);
		btn.setHeight(16);

		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String module = getSelectedModule();
				final String className = getAgentClass(rolloverRecord);
				if (module != null && className != null)
					SC.askforValue("Start new agent", "Runtime name for the new " + className + " agent?",
							new ValueCallback() {
								@Override
								public void execute(String runtimeName) {
									if (runtimeName != null && runtimeName.length() > 0) {
										startNewAgent(module, className, runtimeName);
									}
								}
							});
			}
		});

		rolloverCanvas.addMember(btn);
	}

	private void reloadAgentClasses() {
		classes.setData();
		modules.setData();
		RequestBuilderUtil.get("rest/agents/classes", new RequestCallback() {
			@Override
			public void onResponseReceived(Request req, Response resp) {
				JsArray<AgentClassWrapper> result = JsonUtils.unsafeEval(resp.getText());
				for (int i = 0; i < result.length(); i++)
					addAgent(result.get(i));
				loadModules();
				modules.selectRecord(0);
				updateControls();
			}

			@Override
			public void onError(Request req, Throwable ex) {
				updateControls();
				ex.printStackTrace();
			}
		});
	}

	private void addAgent(AgentClassWrapper agClass) {
		String module = agClass.getModule();
		List<String> agents = module2agents.get(module);
		if (agents == null) {
			agents = new ArrayList<>();
			module2agents.put(module, agents);
		}
		agents.add(agClass.getEjbName());
	}

	private void loadModules() {
		final Set<String> keys = module2agents.keySet();
		ListGridRecord[] recs = new ListGridRecord[keys.size()];
		int i = 0;
		for (String m : keys)
			recs[i++] = createRecord(m);
		modules.setData(recs);
	}

	private void reloadClassesForSelectedModule() {
		List<ListGridRecord> recs = new ArrayList<>();
		String module = getSelectedModule();
		if (module != null) {
			List<String> agClasses = module2agents.get(module);
			if (agClasses != null)
				for (String c : agClasses)
					recs.add(createRecord(c));
		}
		classes.setData(recs.toArray(new ListGridRecord[0]));
	}

	private void startNewAgent(String module, String className, String runtimeName) {
		try {
			String url = "rest/agents/running/" + module + "$" + className + "/" + runtimeName;
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
				JsArray<RunningAgentWrapper> result = JsonUtils.unsafeEval(resp.getText());
				admin.onRunningAgents(result);
				updateControls();
			}

			@Override
			public void onError(Request req, Throwable ex) {
				ex.printStackTrace();
			}
		});
	}

	private ListGridRecord createRecord(String str) {
		ListGridRecord rec = new ListGridRecord();
		rec.setAttribute("name", str);
		return rec;
	}

	private String getSelectedModule() {
		ListGridRecord moduleRec = modules.getSelectedRecord();
		if (moduleRec != null)
			return moduleRec.getAttribute("name");
		return null;
	}

	private String getAgentClass(ListGridRecord rec) {
		if (rec == null)
			return null;
		return rec.getAttribute("name");
	}

	private void updateControls() {

	}
}
