package nz.ac.auckland.parolee.test;

import static org.junit.Assert.fail;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JUnit test case to test the behaviour of the Parolee Web service. 
 * The test basically uses the Web service to create a new Parolee, to query it,
 * to update it and to requery it.
 * 
 * The test is implemented using the JAX-RS client API, which will be covered 
 * later.
 * 
 * @author Ian Warren
 *
 */
public class ParoleeResourceTest
{
	private Logger _logger = LoggerFactory.getLogger(ParoleeResourceTest.class);
			
   @Test
   public void testsPass() {}
   
   @Test
   public void testParoleeResource()
   {
	   // Use ClientBuilder to create a new client that can be used to create 
	   // connections to the Web service.
      Client client = ClientBuilder.newClient();
      try {
         _logger.info("Creating a new Parolee ...");

         // Create a XML representation for a new Parolee.
         String xml = "<parolee>"
                 + "<first-name>Mark</first-name>"
                 + "<last-name>Lundy</last-name>"
                 + "<gender>Male</gender>"
                 + "<date-of-birth>21/03/1956</date-of-birth>"
                 + "</parolee>";

         // Send a HTTP POST message, with a message body containing the XML, 
         // to the Web service.
         Response response = client.target("http://localhost:8080/services/parolees")
                 .request().post(Entity.xml(xml));
         
         // Expect a HTTP 201 "Created" response from the Web service.
         int status = response.getStatus();
         if (status != 201) {
        	 _logger.error("Failed to create Parolee; Web service responded with: " + status);
        	 fail();
         }
         
         // Extract location header from the HTTP response message. This should 
         // give the URI for the newly created Parolee.
         String location = response.getLocation().toString();
         _logger.info("URI for new Parolee: " + location);
         
         // Close the connection to the Web service.
         response.close();

         // Query the Web service for the new Parolee. Send a HTTP GET request.
         _logger.info("Querying the Parolee ...");
         String parolee = client.target(location).request().get(String.class);
         _logger.info("Retrieved Parolee:\n" + parolee);

         // Create a XML representation of the Parolee, changing the value for 
         // date-of-birth.
         String updateParolee = "<parolee>"
                 + "<first-name>Mark</first-name>"
                 + "<last-name>Lundy</last-name>"
                 + "<gender>Male</gender>"
                 + "<date-of-birth>22/03/1956</date-of-birth>"
                 + "</parolee>";
         
         // Send a HTTP PUT request to the Web service. The request URI is 
         // that retrieved from the Web service (the response to the GET message)
         // and the message body is the above XML.
         response = client.target(location).request().put(Entity.xml(updateParolee));
         
         // Expect a HTTP 204 "No content" response from the Web service.
         status = response.getStatus();
         if (status != 204) {
        	 _logger.error("Failed to update Parolee; Web service responded with: " + status);
        	 fail();
         }
         
         // Close the connection.
         response.close();
         
         // Finally, re-query the Parolee. The date-of-birth should have been 
         // updated. 
         _logger.info("Querying the updated Parolee ...");
         parolee = client.target(location).request().get(String.class);
         _logger.info("Retrieved Parolee:\n" + parolee);
      } finally {
    	  // Release any connection resources.
         client.close();
      }
   }
}

