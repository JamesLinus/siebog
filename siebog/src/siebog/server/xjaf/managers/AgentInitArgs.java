package siebog.server.xjaf.managers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.FormParam;
import org.jboss.resteasy.annotations.Form;

/**
 * Wrapper class for agent initialization arguments. See <a
 * href="https://issues.jboss.org/browse/RESTEASY-821">RESTEASY-821</a> for more details.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AgentInitArgs implements Serializable {
	public static class Arg implements Serializable {
		private static final long serialVersionUID = 1L;
		@FormParam("value")
		private String value;

		public Arg() {
		}

		public Arg(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private static final long serialVersionUID = 1L;
	@Form(prefix = "arg")
	private Map<String, Arg> args;

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
			String[] kv = str.split("->");
			args.put(kv[0], new Arg(kv[1]));
		}
	}

	public void put(String key, String value) {
		args.put(key, new Arg(value));
	}

	public String get(String key) {
		Arg arg = args.get(key);
		return arg != null ? arg.value : null;
	}

	public Map<String, String> toStringMap() {
		Map<String, String> map = new HashMap<>(args.size());
		for (Entry<String, Arg> e : args.entrySet())
			map.put(e.getKey(), e.getValue().getValue());
		return map;
	}
}
