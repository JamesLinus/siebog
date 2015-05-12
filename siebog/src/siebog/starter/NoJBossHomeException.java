package siebog.starter;

@SuppressWarnings("serial")
public class NoJBossHomeException extends IllegalStateException {
	public NoJBossHomeException() {
		super("Environment variable JBOSS_HOME not (properly) set.");
	}
}
