package org.aksw.sparqlify.admin.web.api;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

@Path("/api/action")
@Service
public class ServletManager
{
	@Resource(name="sessionFactory")
	private SessionFactory sessionFactory;
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/test")
	public String test() {
		return "{}";
	}

	
	/**
	 * TODO Somehow we need to create servlets from a configurable set of classes.
	 * The main question is how to inject the proper resources - i.e.
	 * So we somehow need to create an appropriate servlet context and servlet mapping object.
	 * This will probably be some fun fiddling around :/
	 * 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/testCreate")
	public String testCreate(@Context HttpServletRequest req, @Context HttpServletResponse res) {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setJdbcUrl("jdbc:postgresql//foobar");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres".toCharArray());
		dataSource.setPrimaryLabel("My datasource");
		dataSource.setPrimaryComment("");

		
		session.saveOrUpdate(dataSource);
		
		tx.commit();
		
		Criteria c = session.createCriteria(JdbcDataSource.class); //.add(Restrictions.eq("", "test spec"));
		List l = c.list();
		for(Object o : l) {
			System.out.println(o);
		}
				
		
		session.close();

		
		
//		res.setContentType("text/plain");
//        res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
//        res.setHeader("Location", "foobar");
        
        
//		Dynamic dynamic = context.addServlet("SNORQL-Namespaces", "com.sun.jersey.spi.spring.container.servlet.SpringServlet");
//		dynamic.addMapping("SNORQL-Namespaces", "/foobar/*");
//		dynamic.setAsyncSupported(true);
//		dynamic.setInitParameter("com.sun.jersey.config.property.packages", "org.aksw.sparqlify.platform.web");
		//dynamic.setLoadOnStartup(1);
		
		
		//context.addServlet("/sparql", HttpSparqlEndpoint.class);
		
		//ServletContext context;
//		ServletHolder sh = new ServletHolder(ServletContainer.class);
//
//		sh.setInitParameter(
//				"com.sun.jersey.config.property.resourceConfigClass",
//				"com.sun.jersey.api.core.PackagesResourceConfig");
//
//		sh.setInitParameter("com.sun.jersey.config.property.packages",
//				"org.aksw.sparqlify.web");
//
//		Server server = new Server(port);
//		ServletContextHandler context = new ServletContextHandler(server, "/",
//				ServletContextHandler.SESSIONS);
//
//		QueryExecutionFactory qef = null;
//		
//		context.getServletContext().setAttribute("queryExecutionFactory", qef);
//		context.addServlet(sh, "/*");
//		
		
		return "{}";
	}

	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create")
	public String createInstance(@FormParam("name") String name) {
		return "{}";
	}

}
