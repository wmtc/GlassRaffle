package net.designxperts.glassraffle;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.designxperts.glassraffle.notifications.TimelineCallbackServlet;

import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.Subscription;
import com.google.api.services.mirror.model.SubscriptionsListResponse;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public final class GlassRaffle {

	
	private static final Logger log = Logger.getLogger(GlassRaffle.class.getName());
	private static final String CALLBACK = "https://glassraffle.appspot.com/timelineupdatecallback";
	

	  public static void subscribe( HttpServletRequest req, String userId )
	      throws IOException
	  {
	    Mirror mirror = MirrorUtils.getMirror( req );

	    // START:subscribe
	   
	    log.warning("IN SUBSCRIBE!!!");
	    Subscription tliSubscription = new Subscription()
	      .setCallbackUrl( CALLBACK )
	      .setVerifyToken( "a_secret_to_everybody" )
	      .setUserToken( userId )
	      .setCollection( "timeline" )
	      .setOperation( Collections.singletonList( "UPDATE" ) );
	    //MENU_ACTION
	    
	    mirror.subscriptions().insert( tliSubscription ).execute();
	    // END:subscribe

	    // TODO: check if this user has subscribed, skip if already has
	    SubscriptionsListResponse subscriptions = mirror.subscriptions().list().execute();
	    for (Subscription sub : subscriptions.getItems()) {
	      System.out.println( sub );
	    }
	  }
	
	public static void insertAndSaveSimpleHtmlTimelineItem( ServletContext ctx, String userId )
			throws IOException, ServletException {
		
		Mirror mirror = MirrorUtils.getMirror( userId );
		Timeline timeline = mirror.timeline();
		log.warning("IN INSERT METHOD!!!");
		// START:insertSimpleHtmlTimelineItem
		// get a cuisine, populate an object, and render the template
		//String cuisine = getRandomCuisine();
		//String text = "Random text";
		Map<String, String> data = Collections.singletonMap( "text", "Let's Raffle!..." );
		
		String html = render( ctx, "glass/card.ftl", data );

		TimelineItem timelineItem = new TimelineItem()
		.setTitle( "Glass Raffle" )
		.setHtml( html );
		
		//.setSpeakableText( "You should eat "+cuisine+" for lunch" );
		//ti.setMenuItems( new LinkedList<MenuItem>() );
		setAllMenuItems(timelineItem);
	    
	    mirror.timeline().insert( timelineItem ).execute();
	    //TimelineItem tiResp = timeline.insert( timelineItem ).execute();
		// END:insertSimpleHtmlTimelineItem
	    
	    
	    
		//setLunchRouletteId( userId, tiResp.getId() );
	}
	
	public static TimelineItem insertRaffleHtmlTimelineItem( ServletContext ctx, String userId, boolean isXE )
			throws IOException, ServletException {
		String winner = "";
		String output = "";
		Map<String, String> data = null;

		Mirror mirror = MirrorUtils.getMirror( userId );
		winner = getRandomWinner(isXE);
		if(!isXE) {
			
			output = "Raffle Winner is: " + winner;
			data = Collections.singletonMap( "text", output );
		}
		else {
			output = "Raffle XE Winner is: " + winner;
			data = Collections.singletonMap( "text", output );
		}
		String html = render( ctx, "glass/card.ftl", data );

		TimelineItem timelineItem = new TimelineItem()
		.setTitle( "Glass Raffle" )
		.setHtml( html );
		//.setSpeakableText( "You should eat "+cuisine+" for lunch" );
		//ti.setMenuItems( new LinkedList<MenuItem>() );
		setDeleteMenuItem(timelineItem);
	    
	    return timelineItem;
	    //TimelineItem tiResp = timeline.insert( timelineItem ).execute();
		// END:insertSimpleHtmlTimelineItem
	    
		//setLunchRouletteId( userId, tiResp.getId() );
	}
	// START:randomWinner
	  public static String getRandomWinner(boolean isXE)
	  {
		  	List<Key> memberIDs = new ArrayList<Key>();
			String winner = null;
			boolean isSelected = false;
			boolean hasGlass = false;
			boolean looking = true;
			Key memberID = null;
			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query q = new Query("Member");
			
			// Use PreparedQuery interface to retrieve results
			PreparedQuery pq = datastore.prepare(q);
			
			for (Entity result : pq.asIterable()) {
				memberIDs.add(result.getKey());	 
			}
			
			while(looking) {
				try {					
					
						int choice = new Random().nextInt(memberIDs.size());
						memberID = memberIDs.get(choice);
						Entity member = datastore.get( memberID );
						isSelected = (boolean)member.getProperty("isSelected");
						hasGlass = (boolean)member.getProperty("hasGlass");
				      
						if(!isSelected && !hasGlass) {
				    	  
							winner =  (String) member.getProperty("name");
							member.setProperty("isSelected", true);
							datastore.put( member );
							looking = false;
						} 
						if(isXE) {
							if(!isSelected && hasGlass) {
						    	  
								winner =  (String) member.getProperty("name");
								member.setProperty("isSelected", true);
								datastore.put( member );
								looking = false;
							} 
						}
				} 
				catch (EntityNotFoundException e) {
					return null;
				}
			}
			return winner + " id: " + memberID;
		  
			//String[] names = { "Rasheed", "Shazafar", "Someone Else" };
			//int choice = new Random().nextInt(names.length);
			//return names[choice];
	  }
	  // END:randomCuisine
	  
	  public static void setDeleteMenuItem(TimelineItem ti) {
			
			ti.setMenuItems(new LinkedList<MenuItem>());
			
		    ti.getMenuItems().add( new MenuItem().setAction( "DELETE" )
		    );
		}
	  
	public static void setAllMenuItems(TimelineItem ti) {
		
		ti.setMenuItems(new LinkedList<MenuItem>());
		
		
		/*
	    // Set up two menu values for DEFAULT and PENDING
	    List<MenuValue> rafflemenuValue = new ArrayList<MenuValue>(2);
	    List<MenuValue> explorerRaffleMenuValue = new ArrayList<MenuValue>(1);
	    
	    rafflemenuValue.add( new MenuValue()
	    	.setState( "DEFAULT" )
	    	.setDisplayName( "Raffle" ) );
	    
	    rafflemenuValue.add( new MenuValue()
        .setState( "PENDING" )
        .setDisplayName( "Generating ..." ) );
	    
	    explorerRaffleMenuValue.add(new MenuValue()
	    	.setState("DEFAUT")
	    	.setDisplayName("Explorer Raffle")		
	    );
	    
	    // Add new CUSTOM menu item
	    ti.getMenuItems().add( new MenuItem()
	    	.setAction( "CUSTOM" )
	    	.setId("RAFFLE")
	    	.setPayload("RAFFLE")
	    	.setValues( rafflemenuValue )
	    );
	    
	    ti.getMenuItems().add(new MenuItem()
	    	.setAction("CUSTOM")
	    	.setId("RAFFLE_XE")
	    	.setPayload("RAFFLE_XE")
	    	.setValues(explorerRaffleMenuValue)
	    );
	    
	    */
	    ti.getMenuItems().add( new MenuItem()
	      .setAction( "CUSTOM" )
	        .setId( "RAFFLE" )
	        .setPayload( "RAFFLE" )
	        .setValues( Collections.singletonList( new MenuValue()
	          .setState( "DEFAULT" )
	          .setDisplayName( "Raffle" )) 
	        )
	      );
	    ti.getMenuItems().add( new MenuItem()
	      .setAction( "CUSTOM" )
	        .setId( "RAFFLE XE" )
	        .setPayload( "RAFFLE XE" )
	        .setValues( Collections.singletonList( new MenuValue()
	          .setState( "DEFAULT" )
	          .setDisplayName( "Raffle XE" ) ) 
	        )
	      );
	    ti.getMenuItems().add(
	      new MenuItem().setAction( "DELETE" )
	    );
	}
	 // START:render
	  public static String render(ServletContext ctx, String template, Object data)
	      throws IOException, ServletException
	  {
	    Configuration config = new Configuration();
	    config.setServletContextForTemplateLoading(ctx, "WEB-INF/views");
	    config.setDefaultEncoding("UTF-8");
	    Template ftl = config.getTemplate(template);
	    try {
	      // use the data to render the template to the servlet output
	      StringWriter writer = new StringWriter();
	      ftl.process(data, writer);
	      return writer.toString();
	    }
	    catch (TemplateException e) {
	      throw new ServletException("Problem while processing template", e);
	    }
	  }
	  // END:render

}
