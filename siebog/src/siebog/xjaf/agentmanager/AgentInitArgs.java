package siebog.xjaf.agentmanager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.FormParam;
import org.jboss.resteasy.annotations.Form;

/**
 * Wrapper class for agent initialization arguments.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AgentInitArgs implements Serializable {
	// cannot use String directly, see https://issues.jboss.org/browse/RESTEASY-821
	public static class StringWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		@FormParam("value")
		public String value;
	}

	private static final long serialVersionUID = 1L;
	@Form(prefix = "arg")
	private Map<String, StringWrapper> args;

	public AgentInitArgs() {
		args = new HashMap<>();
	}

	/**
	 * Accepts an array of strings in form of "key->value".
	 * 
	 * @param keyValues
	 */
	public AgentInitArgs(String... keyValues) {
		args = new HashMap<>(keyValues.length);
		for (String str : keyValues) {
			String[] kv = str.split("=");
			StringWrapper arg = new StringWrapper();
			arg.value = kv[1];
			args.put(kv[0], arg);
		}
	}

	public void put(String key, String value) {
		StringWrapper arg = new StringWrapper();
		arg.value = value;
		args.put(key, arg);
	}

	public String get(String key) {
		StringWrapper arg = args.get(key);
		return arg != null ? arg.value : null;
	}

	public Map<String, String> toStringMap() {
		Map<String, String> map = new HashMap<>(args.size());
		for (Entry<String, StringWrapper> e : args.entrySet())
			map.put(e.getKey(), e.getValue().value);
		return map;
	}
}
