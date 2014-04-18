package net.designxperts.glassraffle;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.designxperts.glassraffle.notifications.TimelineCallbackServlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class StoreMembersServlet extends HttpServlet {
	private static String memberName;
	private static boolean memberHasGlass;
	private static boolean memberIsSelected;
	private static final Logger log = Logger.getLogger(TimelineCallbackServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		memberName = req.getParameter("fullName");
		if(req.getParameter("gotGlass").equals("Y")) {
			memberHasGlass = true;
		}
		else
			memberHasGlass = false;
		
		
		//resp.setContentType("text/plain");
		//resp.getWriter().println("Hello, world");
		
		memberIsSelected = false;
		log.warning("IN STORE MEMBER BEFORE STORING!!!");
		String newMember = storeMember();
		//newMember = newMember.substring(newMember.indexOf('(') + 1, newMember.length() -1);
		if(newMember != null) {
			resp.setContentType("text/plain");
			resp.getWriter().append( "Congrats! you've entered the raffle. \nRemember you must be present to win.\nYour raffle number is:  " + newMember.substring(newMember.indexOf('(') + 1, newMember.length() -1));
			
		}
	}
	public static String storeMember()  {
	    // START:setLunchRouletteId
	    DatastoreService store = DatastoreServiceFactory.getDatastoreService();
	    //Key key = KeyFactory.createKey( LunchRoulette.class.getSimpleName(), userId );
	    Entity member = new Entity( "Member" );
	    member.setProperty( "name", memberName );
	    member.setProperty("hasGlass", memberHasGlass);
	    member.setProperty("isSelected",memberIsSelected);
	    store.put( member );
	    return member.getKey().toString();
	    
	    // END:setLunchRouletteId
	    //return true;
	  }

}
