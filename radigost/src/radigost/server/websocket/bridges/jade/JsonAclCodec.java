package radigost.server.websocket.bridges.jade;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class JsonAclCodec
{
	public static ACLMessage decode(String jsonStr) throws Exception
	{
		JSONObject obj = (JSONObject) JSONValue.parse(jsonStr);
		
		// performative
		final Long i = (Long) obj.get("performative");
		final RadigostPerformative[] performatives = RadigostPerformative.values();
		if ((i == null) || (i < 0) || (i >= performatives.length))
			throw new Exception("Missing or invalid performative: " + jsonStr);
		ACLMessage acl = new ACLMessage(performatives[i.intValue()].getId());
		
		// sender
		JSONObject sender = (JSONObject) obj.get("sender");
		if (sender != null)
			acl.setSender(new AID((String) sender.get("value"), true));
		
		// receivers
		JSONArray receivers = (JSONArray) obj.get("receivers");
		for (Object robj : receivers)
		{
			JSONObject rcvr = (JSONObject) robj;
			acl.addReceiver(new AID((String) rcvr.get("value"), true));
		}
		
		// reply-to
		JSONArray replyTo = (JSONArray) obj.get("replyTo");
		if (replyTo != null)
			for (Object robj : replyTo)
			{
				JSONObject rrep = (JSONObject) robj;
				acl.addReplyTo(new AID((String) rrep.get("value"), true));
			}
		
		// TODO : add support for binary content; see impl. of StringACLConent
		Object content = obj.get("content");
		if (content != null)
			acl.setContent(content.toString());
		// replyWith
		String str = (String) obj.get("replyWith");
		if ((str != null) && (str.length() > 0))
			acl.setReplyWith(str);
		// inReplyTo
		str = (String) obj.get("inReplyTo");
		if ((str != null) && (str.length() > 0))
			acl.setInReplyTo(str);
		// encoding
		str = (String) obj.get("encoding");
		if ((str != null) && (str.length() > 0))
			acl.setEncoding(str);
		// language
		str = (String) obj.get("language");
		if ((str != null) && (str.length() > 0))
			acl.setLanguage(str);
		// ontology
		str = (String) obj.get("ontology");
		if ((str != null) && (str.length() > 0))
			acl.setOntology(str);
		// protocol
		str = (String) obj.get("protocol");
		if ((str != null) && (str.length() > 0))
			acl.setProtocol(str);
		// conversationId
		str = (String) obj.get("conversationId");
		if ((str != null) && (str.length() > 0))
			acl.setConversationId(str);

		// TODO : test json-to-date conversion
		Date date = (Date) obj.get("replyBy");
		if (date != null)
			acl.setReplyByDate(date);
		
		return acl;
	}
	
	@SuppressWarnings("unchecked")
	public static String encode(ACLMessage acl, List<String> receivers) throws Exception
	{
		JSONObject obj = new JSONObject();
		
		// performative
		String str = ACLMessage.getPerformative(acl.getPerformative()).replace('-', '_');
		RadigostPerformative perf = RadigostPerformative.valueOf(str.toUpperCase());
		obj.put("performative", perf.ordinal());
		
		// sender
		if (acl.getSender() != null)
			obj.put("sender", aid2obj(acl.getSender(), true));
		
		// receivers
		JSONArray array = encodeArray(acl.getAllReceiver(), receivers, false);
		obj.put("receivers", array);
		
		// reply-to
		array = encodeArray(acl.getAllReplyTo(), null, true);
		if (array.size() > 0)
			obj.put("replyTo", array);
		
		// TODO : add support for binary content; see impl. of StringACLConent
		put(obj, "content", acl.getContent());
		put(obj, "replyWith", acl.getReplyWith());
		put(obj, "inReplyTo", acl.getInReplyTo());
		put(obj, "encoding", acl.getEncoding());
		put(obj, "language", acl.getLanguage());
		put(obj, "ontology", acl.getOntology());
		put(obj, "protocol", acl.getProtocol());
		put(obj, "conversationId", acl.getConversationId());
		
		// TODO : what is the date format expected by JS?
		Date date = acl.getReplyByDate();
		if (date != null)
			obj.put("replyBy", date);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	private static void put(JSONObject obj, String name, String value)
	{
		if ((value != null) && (value.length() > 0))
			obj.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	private static JSONArray encodeArray(Iterator<?> i, List<String> list, boolean server)
	{
		JSONArray array = new JSONArray();
		while (i.hasNext())
		{
			AID aid = (AID) i.next();
			array.add(aid2obj(aid, server));
			if (list != null)
				list.add(aid.getName());
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject aid2obj(AID aid, boolean server)
	{
		JSONObject obj = new JSONObject();
		obj.put("name", aid.getLocalName());
		obj.put("device", aid.getHap());
		obj.put("value", aid.getName());
		obj.put("server", server);
		return obj;
	}
}
