package siebog.server.radigost;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import siebog.server.radigost.entities.AgentState;
import siebog.server.radigost.websocket.bridges.BridgeException;

@Path("/")
public class RestService
{
	private static final Logger logger = Logger.getLogger(RestService.class.getName());
	@PersistenceContext(name = "Radigost")
	private EntityManager em;

	@PUT
	@Path("/bridge")
	public Response createBridge(@QueryParam("name") String name, @QueryParam("host") String host)
	{
		try
		{
			Global.getBridgeManager().runBridge(name, host);
		} catch (IllegalArgumentException ex)
		{
			logger.info("Bridge [" + name + "] already running.");
		} catch (BridgeException | NamingException ex)
		{
			logger.log(Level.WARNING, "Error while creating a bridge.", ex);
			String msg = ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
		}
		return Response.status(Status.OK).build();
	}
	
	@GET
	@Path("/agentState/{aid}")
	public Response getAgentState(@PathParam("aid") String aid)
	{
		AgentState state = em.find(AgentState.class, aid);
		if (state != null)
			return Response.status(Status.OK).entity(state.getState()).build();
		return Response.status(Status.NOT_FOUND).build();
	}
	
	@POST
	@Path("/agentState/{aid}/{state}")
	public Response setAgentState(@PathParam("aid") String aid, @PathParam("state") String state)
	{
		AgentState obj = em.find(AgentState.class, aid);
		if (obj == null)
		{
			obj = new AgentState();
			obj.setAid(aid);
			obj.setState(state);
			em.persist(obj);
		}
		else
		{
			obj.setState(state);
			em.merge(obj);
		}
		return Response.status(Status.OK).build();
	}
}
