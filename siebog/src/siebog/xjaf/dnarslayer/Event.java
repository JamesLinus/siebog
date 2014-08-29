package siebog.xjaf.dnarslayer;

import java.io.Serializable;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

	public static enum Kind {
		ADDED, UPDATED
	}

	private final Kind kind;
	private final String statement;

	public Event(Kind kind, String statement) {
		this.kind = kind;
		this.statement = statement;
	}

	public Kind getKind() {
		return kind;
	}

	public String getStatement() {
		return statement;
	}

	@Override
	public String toString() {
		return kind + " " + statement;
	}
}
