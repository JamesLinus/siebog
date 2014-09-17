package siebog.agents.jason;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import jason.architecture.AgArch;
import siebog.jasonee.RemoteObjectFactory;
import siebog.jasonee.control.UserExecutionControl;

@Stateless
@Remote(RemoteObjectFactory.class)
public class ExampleRemoteObjectFactory implements RemoteObjectFactory {
	private static final long serialVersionUID = 1L;

	@Override
	public AgArch createAgArch(String className) {
		return new MyAgentArch();
	}

	@Override
	public UserExecutionControl createExecutionControl(String className) throws Exception {
		throw new IllegalArgumentException("Invalid class name: " + className);
	}
}
