package org.opennms.web.category;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import org.opennms.web.MissingParameterException;


public class RTCDebugServlet extends HttpServlet
{
    protected CategoryModel model;
    
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
    
    
    public void doGet( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter( "category" );
        
        if( categoryName == null ) {
            categoryName = CategoryModel.OVERALL_AVAILABILITY_CATEGORY;
        }
              
        response.setContentType("text/plain");      
        PrintWriter out = response.getWriter();

        Category category = model.getCategory(categoryName);    
        
        if( category == null ) {
            out.write( "No data exists for this category.  Please check your spelling of the category name." );
        }
        else {
            try {                
                Marshaller.marshal( category.getRtcCategory(), out );
            }
            catch( MarshalException e ) {
                throw new ServletException( "Could not marshal the RTC info", e );
            }
            catch( ValidationException e ) {
                throw new ServletException( "Could not marshal the RTC info", e );
            }                
        }

        out.close();                
    }

}
     
