package siebog.interaction.bsp;

import java.io.Serializable;

public class Superstep implements Serializable {
	private static final long serialVersionUID = 1L;
	private final long counter;

	public Superstep(long counter) {
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}
}
