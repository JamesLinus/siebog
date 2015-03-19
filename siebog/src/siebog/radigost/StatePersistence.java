package siebog.radigost;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/state")
public class StatePersistence {
	@Inject
	private Cassandra cassandra;

	@GET
	@Path("/{aid}")
	public String get(@PathParam("aid") String aid) {
		return cassandra.getState(aid);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/")
	public void set(@FormParam("aid") String aid, @FormParam("state") String state) {
		cassandra.setState(aid, state);
	}
}
