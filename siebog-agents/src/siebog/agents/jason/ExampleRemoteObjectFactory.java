package siebog.agents.jason;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import siebog.jasonee.JasonEEAgArch;
import siebog.jasonee.RemoteObjectFactory;
import siebog.jasonee.control.UserExecutionControl;
import siebog.jasonee.environment.UserEnvironment;

@Stateless
@Remote(RemoteObjectFactory.class)
public class ExampleRemoteObjectFactory implements RemoteObjectFactory {
	private static final long serialVersionUID = 1L;

	@Override
	public JasonEEAgArch createAgArch(String className) {
		throw new IllegalArgumentException("Invalid class name: " + className);
	}

	@Override
	public UserExecutionControl createExecutionControl(String className) {
		throw new IllegalArgumentException("Invalid class name: " + className);
	}

	@Override
	public UserEnvironment createEnvironment(String className) {
		throw new IllegalArgumentException("Invalid class name: " + className);
	}
}
