package net.designxperts.glassraffle;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;


@SuppressWarnings("serial")
public class GlassRaffleServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		
		//resp.setContentType("text/plain");
		//resp.getWriter().println("Hello, world");
		
		ServletContext ctx = getServletContext();
		String userId = SessionUtils.getUserId( req );
		GlassRaffle.insertAndSaveSimpleHtmlTimelineItem(ctx, userId);
		
		resp.setContentType("text/plain");
		resp.getWriter().append( "Inserted Timeline Item");
	}
}
