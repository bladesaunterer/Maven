package nz.ac.auckland.audit;

import java.io.IOException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Auditor object acts as an interceptor on HTTP requests that arrive in a 
 * servlet container. The Auditor examines the request and stores a description 
 * of it in the database. Specifically, the Auditor stores the following for each
 * request:
 * - the CRUD operator
 * - the time at which the request was made
 * - the URI that was invoked
 * - the user (if known) who made the request
 * 
 * The Auditor assumes the existence of two Entity classes, AuditEntry and User, 
 * that are appropriately decorated with persistence metadata.
 * 
 * Additionally, the Auditor assumes that requests may include a "username" 
 * cookie. When present, the Auditor uses the username value when persisting the
 * user. Otherwise, a value of UNKNOWN is recorded for the user.
 * 
 * @author Ian Warren
 *
 */
public class Auditor implements ContainerRequestFilter {
	private static String DEFAULT_USERNAME = "UNKNOWN";

	private static Logger _logger = LoggerFactory.getLogger(Auditor.class);
	
	/**
	 * When the Auditor is registered, this method will be called automatically
	 * by the JAX-RS run-time when an incoming HTTP request is received and
	 * prior to it being forwarded for processing to a Web service resource.
	 */
	@Override
	public void filter(ContainerRequestContext cxt) throws IOException {
		_logger.debug("Auditor is filtering");
		
		// Read the value of the cookie named "username". If there is no such
		// cookie, use DEFAULT_USERNAME.
		Map<String,Cookie> cookies = cxt.getCookies();
		String username = DEFAULT_USERNAME;
		Cookie cookie = cookies.get("username");
		if(cookie != null) {
			username = cookie.getValue();
		}

		EntityManager em = Persistence.createEntityManagerFactory("auditorPU")
				.createEntityManager();
		
		// Start a transaction for persisting the audit data.
		em.getTransaction().begin();
		
		// Fetch the User object corresponding to username. If there is no such
		// user with that name, create a new User.
		User user = fetchUser(em, username);
		
		// Create a new AuditEntry object to store details about the Web service
		// invocation.
		AuditEntry.CrudOperation crudOp = null;
		Request request = cxt.getRequest();
		String httpMethod = request.getMethod();
		if(httpMethod.equalsIgnoreCase("GET")) {
			crudOp = AuditEntry.CrudOperation.Retrieve;
		} else if(httpMethod.equalsIgnoreCase("POST")) {
			crudOp = AuditEntry.CrudOperation.Create;
		} else if(httpMethod.equalsIgnoreCase("PUT")) {
			crudOp = AuditEntry.CrudOperation.Update;
		} else if(httpMethod.equalsIgnoreCase("DELETE")) {
			crudOp = AuditEntry.CrudOperation.Delete;
		}
		String uri = cxt.getUriInfo().getRequestUri().toString();
		AuditEntry auditable = new AuditEntry(crudOp, uri, user);
		
		// Persist the AuditEntry in the database.
		em.persist(auditable);
		em.getTransaction().commit();
	}
	
	protected User fetchUser(EntityManager em, String username) {
		User user = null;
		try {
			_logger.debug("Querying the database for the current user");
			TypedQuery<User> query = em.createQuery(
				"select u from User u where u._username = :username", User.class
				).setParameter("username", username);
			user = query.getSingleResult();
		} catch(NoResultException e) {
			// User doesn't exist in the database, so create a new User with the
			// given username.
			_logger.debug("Adding new user to the database: " + username);
			user = new User(username);
		}
		return user;
	}
}
