package nz.ac.auckland.parolee.services;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Service interface for the Parolees application. This interface allows 
 * Parolees to be created, queried (by id) and updated.
 * 
 * @author Ian Warren
 *
 */
@Path("/parolees")
public interface ParoleeResource {

	/**
	 * Handles incoming HTTP POST requests for the relative URI "parolees", 
	 * where the request body contains XML data. This method creates new 
	 * Parolees.
	 * @param stores the HTTP request message body. This is a regular 
	 * java.io.InputStream object and is expected to hold a XML representation
	 * of the Parolee to create, 
	 * @return a JAX-RS Response object that the JAX-RS implementation uses to
	 * prepare the HTTP response message.
	 */
   @POST
   @Consumes("application/xml")
   Response createParolee(InputStream is);


   /**
    * Handles incoming HTTP GET requests for the relative URI "parolees/{id}.
    * @param id the unique id of the Parolee to retrieve.
    * @return a StreamingOutput object storing a representation of the required
    *         Parolee in XML format.
    */
   @GET
   @Path("{id}")
   @Produces("application/xml")
   StreamingOutput retrieveParolee(@PathParam("id") int id);

   /**
    * Handles incoming HTTP PUT requests for the relative URI "parolees/{id}.
    * @param id the unique id of the Parolee to retrieve.
    * @param is stores the HTTP request message body, which is expected to hold
    * a XML representation of the updated Parolee.
    */
   @PUT
   @Path("{id}")
   @Consumes("application/xml")
   void updateParolee(@PathParam("id") int id, InputStream is);
}
