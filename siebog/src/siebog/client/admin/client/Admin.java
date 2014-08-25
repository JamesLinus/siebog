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

package siebog.client.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Admin implements EntryPoint {
	private ListGrid classes;
	private ListGrid running;
	private IButton reloadClasses;
	private IButton start;
	private IButton reloadRunning;
	private MessagingForm msgForm;

	@Override
	public void onModuleLoad() {
		HLayout root = new HLayout();
		root.setWidth100();
		root.setHeight(600);
		root.setAlign(Alignment.CENTER);

		HLayout ag = new HLayout();
		ag.setWidth100();
		ag.setHeight("50%");
		ag.setMembersMargin(8);
		ag.addMembers(makeClasses(), makeRunning());

		msgForm = new MessagingForm();

		VLayout v = new VLayout();
		v.setWidth(800);
		v.setMembersMargin(8);

		v.addMembers(ag, msgForm.get());

		reloadClasses();
		startGuiAgent();
		reloadRunning();
		updateControls();
		root.addMember(v);
		root.draw();
	}

	private Canvas makeClasses() {
		classes = createGrid();

		ListGridField module = new ListGridField("module", "Module");
		ListGridField ejbName = new ListGridField("ejbName", "Class");
		classes.setFields(module, ejbName);

		reloadClasses = new IButton("Reload", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reloadClasses();
			}
		});
		start = new IButton("Start", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord rec = classes.getSelectedRecord();
				if (rec != null)
					SC.askforValue("Start new agent", "Runtime name for the new agent?", new ValueCallback() {
						@Override
						public void execute(String value) {
							if (value != null) {
								String agClass = rec.getAttribute("module") + '$' + rec.getAttribute("ejbName");
								startNewAgent(agClass, value);
							}
						}
					});
			}
		});

		HLayout controls = new HLayout();
		controls.setMembersMargin(16);
		controls.addMembers(reloadClasses, start);

		VLayout v = new VLayout();
		v.setWidth("50%");
		v.addMember(controls);
		v.addMember(classes);
		return v;
	}

	private Canvas makeRunning() {
		running = createGrid();

		ListGridField name = new ListGridField("id", "Name");
		running.setFields(name);

		reloadRunning = new IButton("Reload", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reloadRunning();
			}
		});

		HLayout controls = new HLayout();
		controls.setMembersMargin(16);
		controls.addMembers(reloadRunning);

		VLayout v = new VLayout();
		v.setWidth("50%");
		v.addMembers(controls, running);
		return v;
	}

	private ListGrid createGrid() {
		ListGrid grid = new ListGrid();
		grid.setShowAllRecords(true);
		grid.setHeight100();
		grid.setCanEdit(false);
		grid.addSelectionChangedHandler(new SelectionChangedHandler() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				updateControls();
			}
		});
		return grid;
	}

	private void reloadClasses() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/agents/classes");
		builder.setHeader("Content-Type", "application/json");
		try {
			builder.sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					JsArray<JavaScriptObject> result = JsonUtils.unsafeEval(resp.getText());
					final int n = result.length();
					ListGridRecord[] rec = new ListGridRecord[n];
					for (int i = 0; i < n; i++)
						rec[i] = new ListGridRecord(result.get(i));
					classes.setData(rec);
					updateControls();
				}

				@Override
				public void onError(Request req, Throwable ex) {
					updateControls();
					ex.printStackTrace();
				}
			});
		} catch (RequestException ex) {
			ex.printStackTrace();
		}
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
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/agents/running");
		builder.setHeader("Content-Type", "application/json");
		try {
			builder.sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					JsArray<AIDWrapper> result = JsonUtils.unsafeEval(resp.getText());
					final int n = result.length();
					ListGridRecord[] rec = new ListGridRecord[n];
					for (int i = 0; i < n; i++)
						rec[i] = new ListGridRecord(result.get(i));
					running.setData(rec);
					msgForm.onRunningList(result);
					updateControls();
				}

				@Override
				public void onError(Request req, Throwable ex) {
					updateControls();
					ex.printStackTrace();
				}
			});
		} catch (RequestException ex) {
			ex.printStackTrace();
		}
	}

	private void updateControls() {
		start.setDisabled(classes.getSelectedRecord() == null);
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
