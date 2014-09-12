package siebog.agents.jason;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import jason.architecture.AgArch;
import siebog.jasonee.JasonEEInfraBuilder;

@Stateless
@Remote(JasonEEInfraBuilder.class)
public class JasonEEExamplesInfraBuilder implements JasonEEInfraBuilder {
	private static final long serialVersionUID = 1L;

	@Override
	public AgArch createAgArch(String className) {
		return new MyAgentArch();
	}
}
