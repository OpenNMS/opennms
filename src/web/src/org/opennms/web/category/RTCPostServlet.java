package org.opennms.web.category;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.utils.ThreadCategory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class RTCPostServlet extends HttpServlet
{
    protected CategoryModel model;
    protected org.apache.log4j.Category log = ThreadCategory.getInstance("RTC");

    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
        }
        catch( IOException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( MarshalException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( ValidationException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }                
    }
    
    
    public void doPost( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //the path info will be the category name we need
        String pathInfo = request.getPathInfo();
        
        //send 400 Bad Request if they did not specify a category in the path info
        if( pathInfo == null ) {
            this.log.error("Request with no path info");
            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "No Category name given in path" );
            return;
        }
        
        //remove the preceding slash if present
        if(pathInfo.startsWith( "/" )) {
            pathInfo = pathInfo.substring(1, pathInfo.length());
        }
        
        //since these category names can contain spaces, etc, 
        //we have to URL encode them in the URL
        String categoryName = URLDecoder.decode(pathInfo);

        org.opennms.netmgt.xml.rtc.Category category = null;
    
        try
        {
            ServletInputStream inStream = request.getInputStream();
            
            //note the unmarshaller closes the input stream, so don't try to close
            //it again or the servlet container will complain                        
            org.opennms.netmgt.xml.rtc.EuiLevel level = (org.opennms.netmgt.xml.rtc.EuiLevel)Unmarshaller.unmarshal(org.opennms.netmgt.xml.rtc.EuiLevel.class, new InputStreamReader(inStream));

            //for now we only deal with the first category, they're only sent one
            //at a time anyway
            category = level.getCategory(0);
        }
        catch(MarshalException ex)
        {
            this.log.error("Failed to load configuration", ex);
            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid XML input" );
            return;
        }
        catch(ValidationException ex)
        {
            this.log.error("Failed to load configuration", ex);
            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid XML input" );
            return;
        }

        //make sure we got data for the category we are interested in
        //send 400 Bad Request if they did not supply category information 
        //for the categoryname in the path info
        if( !categoryName.equals(category.getCatlabel()) ) {
            this.log.error("Request did not supply information for category specified in path info");
            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "No category info found for " + categoryName );
            return;
        }
        
        //update the category information in the CategoryModel
        this.model.updateCategory(category);

        //return a success message
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println( "Category data parsed successfully." );
        out.close();
        
        this.log.info( "Successfully received information for " + categoryName );
    }

}
     
