package nz.ac.auckland.parolee.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import nz.ac.auckland.parolee.domain.Parolee;
import nz.ac.auckland.parolee.domain.Gender;

/**
 * Implementation of the ParoleeResource interface.
 * 
 * @author Ian Warren
 *
 */
public class ParoleeResourceImpl implements ParoleeResource {
	// Setup a Logger.
	private static Logger _logger = LoggerFactory.getLogger(ParoleeResourceImpl.class);
	
	// Thread-safe data structure. This is necessary because a single 
	// ParoleeResourceImpl instance will be created and used to handle all
	// incoming requests. The JAX-RS implementation uses a thread-per-request 
	// model and so concurrent requests will concurrently access the 
	// ParoleeResourceImpl object.
	private Map<Integer, Parolee> _paroleeDB = new ConcurrentHashMap<Integer, Parolee>();
	private AtomicInteger _idCounter = new AtomicInteger();

	@Override
	public Response createParolee(InputStream is) {
		// Read an XML representation of a new Parolee. Note that any non-
		// annotated parameter in a Resource method is assumed to hold the HTTP
		// request's message body.
		Parolee parolee = readParolee(is);
		
		// Generate an ID for the new Parolee, and store it in memory.
		parolee.setId(_idCounter.incrementAndGet());
		_paroleeDB.put(parolee.getId(), parolee);
		
		_logger.info("Created parolee with id: " + parolee.getId());
		
		return Response.created(URI.create("/parolees/" + parolee.getId()))
				.build();

	}

	@Override
	public StreamingOutput retrieveParolee(@PathParam("id") int id) {
		// Lookup the Parolee within the in-memory data structure.
		final Parolee parolee = _paroleeDB.get(id);
		if (parolee == null) {
			// Return a HTTP 404 response if the specified Parolee isn't found.
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		
		// Return a StreamingOuput instance that the JAX-RS implementation will
		// use set the body of the HTTP response message.
		return new StreamingOutput() {
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				outputParolee(outputStream, parolee);
			}
		};
	}

	@Override
	public void updateParolee(@PathParam("id") int id, InputStream is) {
		Parolee update = readParolee(is);
		Parolee current = _paroleeDB.get(id);
		if (current == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);

		// Update the details of the Parolee to be updated.
		current.setFirstname(update.getFirstname());
		current.setLastname(update.getLastname());
		current.setGender(update.getGender());
		current.setDateOfBirth(update.getDateOfBirth());
	}

	// Helper method to generate an XML representation of a particular Parolee. 
	protected void outputParolee(OutputStream os, Parolee parolee)
			throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		String dateOfBirth = formatter.print(parolee.getDateOfBirth());

		PrintStream writer = new PrintStream(os);
		writer.println("<parolee id=\"" + parolee.getId() + "\">");
		writer.println("   <first-name>" + parolee.getFirstname()
				+ "</first-name>");
		writer.println("   <last-name>" + parolee.getLastname()
				+ "</last-name>");
		writer.println("   <gender>" + parolee.getGender() + "</gender>");
		writer.println("   <date-of-birth>" + dateOfBirth + "</date-of-birth>");
		writer.println("</parolee>");
	}

	// Helper method to read an XML representation of a Parolee, and return a
	// corresponding Parolee object. This method uses the org.w3c API for 
	// parsing XML. The details aren't important, and later we'll use an 
	// automated approach rather than having to do this by hand. Currently this
	// is a minimal Web service and so we'll parse the XML by hand.
	protected Parolee readParolee(InputStream is) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(is);
			Element root = doc.getDocumentElement();

			Parolee parolee = new Parolee();
			if (root.getAttribute("id") != null
					&& !root.getAttribute("id").trim().equals(""))
				parolee.setId(Integer.valueOf(root.getAttribute("id")));
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if (element.getTagName().equals("first-name")) {
					parolee.setFirstname(element.getTextContent());
				} else if (element.getTagName().equals("last-name")) {
					parolee.setLastname(element.getTextContent());
				} else if (element.getTagName().equals("gender")) {
					parolee.setGender(Gender.fromString(element
							.getTextContent()));
				} else if (element.getTagName().equals("date-of-birth")) {
					DateTimeFormatter formatter = DateTimeFormat
							.forPattern("dd/MM/yyyy");
					DateTime dateOfBirth = formatter.parseDateTime(element
							.getTextContent());
					parolee.setDateOfBirth(dateOfBirth);
				}
			}
			return parolee;
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		}
	}

}
