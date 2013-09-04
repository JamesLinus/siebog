package org.xjaf2x.client.jason;

import java.util.List;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.AgentManagerI;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.runtime.Settings;

public class Xjaf2xAgArch extends AgArch
{
	private JasonAgentI remote;
	
	@Override
	public void init() throws Exception
	{
		final Settings stts = getTS().getSettings();
		AgentManagerI agentManager = JndiManager.getAgentManager();
		remote = agentManager.startJasonAgent(stts.getUserParameter("family"), getAgName());
		remote.init(stts.getUserParameters());
	}
	
	@Override
	public void stop()
	{
		super.stop();
		remote.terminate();
	}
	
	@Override
	public List<Literal> perceive()
	{
		return remote.perceive();
	}
	
	@Override
	public void act(ActionExec action, List<ActionExec> feedback)
	{
		if (remote.act(action.getActionTerm().getFunctor()))
		{
			action.setResult(true);
			feedback.add(action);
		}
	}
}
