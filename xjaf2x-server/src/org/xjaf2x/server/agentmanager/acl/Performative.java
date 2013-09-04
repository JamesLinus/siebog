package org.xjaf2x.server.agentmanager.acl;

/**
 * Represents FIPA ACL message performatives.
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
public enum Performative
{
	ACCEPT_PROPOSAL,
	AGREE, 
	CANCEL, 
	CALL_FOR_PROPOSAL, 
	CONFIRM, 
	DISCONFIRM, 
	FAILURE, 
	INFORM, 
	INFORM_IF, 
	INFORM_REF, 
	NOT_UNDERSTOOD, 
	PROPAGATE, 
	PROPOSE, 
	PROXY, 
	QUERY_IF, 
	QUERY_REF,
	REFUSE, 
	REJECT_PROPOSAL, 
	REQUEST, 
	REQUEST_WHEN, 
	REQUEST_WHENEVER, 
	SUBSCRIBE
}
