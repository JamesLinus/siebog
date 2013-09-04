package org.xjaf2x.server.agentmanager.agent.jason;

import jason.asSyntax.Literal;
import java.util.List;
import java.util.Map;
import org.xjaf2x.server.agentmanager.agent.AgentI;

public interface JasonAgentI extends AgentI
{
	void init(Map<String, Object> args) throws Exception;
	
	List<Literal> perceive();
	
	boolean act(String functor);
}
