package siebog.interaction.bsp;

import java.io.Serializable;

public class Superstep implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String barrierName;
	private final long counter;

	public Superstep(String barrierName, long counter) {
		this.barrierName = barrierName;
		this.counter = counter;
	}

	public String getBarrierName() {
		return barrierName;
	}

	public long getCounter() {
		return counter;
	}
}
