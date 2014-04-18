package net.designxperts.glassraffle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class SelectWinnerServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		List<String> memberIDs = new ArrayList<String>();
		String key = null;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Member");
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		
		for (Entity result : pq.asIterable()) {
			key = result.getKey().toString();
			memberIDs.add(key.substring(key.indexOf('(') + 1, key.length() -1));	 
		}
		/*
		for(String key : memberIDs) {
			//key = key.substring(key.indexOf('(') + 1, key.length() -1);
			resp.setContentType("text/plain");
			resp.getWriter().append( "" + key + "\n");
			resp.getWriter().append(key.substring(key.indexOf('(') + 1, key.length() -1) + "\n");
		}
		*/
	}

}
