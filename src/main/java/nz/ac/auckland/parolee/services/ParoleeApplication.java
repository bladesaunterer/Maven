package nz.ac.auckland.parolee.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Application class for the Parolee Web service. This class is required by the 
 * JAX-RS implementation to deploy the Web service.
 *  
 * The relative path of the Web service beings with "services". If the Web 
 * server's address is "http://localhost:8080", the URI for hosted Web services
 * thus begins "http://localhost:8080/services". 
 * 
 * The ParoleeResource specifies a URI path of "parolees", so this is appended, 
 * making the Parolees Web service URI "http://localhost:8080/services/parolees".
 * 
 * The ParoleeApplication has only one Resource class (ParoleeResourceImpl) and 
 * this is to be deployed as a singleton, encapsulating a list of Parolee 
 * objects maintained by the Web service.
 *  
 * @author Ian Warren
 *
 */
@ApplicationPath("/services")
public class ParoleeApplication extends Application
{
   private Set<Object> singletons = new HashSet<Object>();

   public ParoleeApplication()
   {
      singletons.add(new ParoleeResourceImpl());
   }

   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }
}
