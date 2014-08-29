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

import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.smartgwt.client.types.FormMethod;
import com.smartgwt.client.types.MultipleAppearance;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextItem;

/**
 * An HTML form for posting ACLMessages.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MessagingForm {
	private static final Logger logger = Logger.getLogger(MessagingForm.class.getName());
	private DynamicForm form;
	private SelectItem sender;
	private SelectItem receivers;
	private SubmitItem send;
	private SelectItem replyTo;
	private SelectItem performative;
	private TextItem content;
	private TextItem language;
	private TextItem encoding;
	private TextItem ontology;
	private TextItem protocol;
	private TextItem conversationId;
	private TextItem replyBy;
	private TextItem replyWith;

	public MessagingForm() {
		performative = new SelectItem("performative", "Performative");
		sender = new SelectItem("sender", "Sender");
		receivers = new SelectItem("receivers", "Receivers");
		receivers.setMultiple(true);
		receivers.setMultipleAppearance(MultipleAppearance.PICKLIST);
		replyTo = new SelectItem("replyTo", "Reply to");
		content = new TextItem("content", "Content");
		language = new TextItem("language", "Language");
		encoding = new TextItem("encoding", "Encoding");
		ontology = new TextItem("ontology", "Ontology");
		protocol = new TextItem("protocol", "Protocol");
		conversationId = new TextItem("conversationId", "Conversation ID");
		replyWith = new TextItem("replyWith", "Reply with");
		replyBy = new TextItem("replyBy", "Reply by");
		send = new SubmitItem("send", "Send");

		form = new DynamicForm();
		form.setHeight("50%");
		form.setMethod(FormMethod.POST);
		form.setAction("rest/messages");
		form.setCanSubmit(true);
		form.setFields(performative, sender, receivers, replyTo, content, language, encoding, ontology, protocol,
				conversationId, replyWith, replyBy, send);

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "rest/messages");
		builder.setHeader("Content-Type", "application/json");
		try {
			builder.sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					JsArray<JavaScriptObject> result = JsonUtils.unsafeEval(resp.getText());
					LinkedHashMap<String, String> map = new LinkedHashMap<>();
					for (int i = 0; i < result.length(); i++) {
						final String p = result.get(i).toString();
						map.put(p, p);
					}
					performative.setValueMap(map);
				}

				@Override
				public void onError(Request req, Throwable ex) {
					logger.log(Level.WARNING, "Error while getting the list of performatives.", ex);
				}
			});
		} catch (RequestException ex) {
			logger.log(Level.WARNING, "Error while getting the list of performatives.", ex);
		}
	}

	public DynamicForm get() {
		return form;
	}

	public void onRunningList(JsArray<AIDWrapper> running) {
		final int n = running.length();
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("", "");
		for (int i = 0; i < n; i++) {
			String id = running.get(i).getId();
			map.put(id, id);
		}
		sender.setValueMap(map);
		receivers.setValueMap(map);
		replyTo.setValueMap(map);
	}
}
