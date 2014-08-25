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

import java.util.LinkedHashMap;
import com.google.gwt.core.client.JsArray;
import com.smartgwt.client.types.FormMethod;
import com.smartgwt.client.types.MultipleAppearance;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;

/**
 * An HTML form for posting ACLMessages.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class MessagingForm {
	private DynamicForm form;
	private SelectItem sender;
	private SelectItem receivers;
	private SubmitItem send;

	public MessagingForm() {
		sender = new SelectItem("sender", "Sender");
		receivers = new SelectItem("receivers", "Receivers");
		receivers.setMultiple(true);
		receivers.setMultipleAppearance(MultipleAppearance.PICKLIST);
		send = new SubmitItem("send", "Send");

		form = new DynamicForm();
		form.setHeight("50%");
		form.setMethod(FormMethod.POST);
		form.setAction("rest/messages");
		form.setCanSubmit(true);
		form.setFields(sender, receivers, send);
	}

	public DynamicForm get() {
		return form;
	}

	public void onRunningList(JsArray<AIDWrapper> running) {
		final int n = running.length();
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			String id = running.get(i).getId();
			map.put(id, id);
		}
		sender.setValueMap(map);
		receivers.setValueMap(map);
	}
}
