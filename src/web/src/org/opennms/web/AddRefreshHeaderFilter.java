package org.opennms.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;


/**
 * A filter that adds an HTTP <em>Refresh</em> header to a servlet or 
 * JSP's response.  The amount of time to wait before refresh is configurable.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddRefreshHeaderFilter extends Object implements Filter
{
    protected FilterConfig filterConfig;
    protected String seconds = "108000";  //default is 30 mins


    /**
     * Adds a <em>Refresh</em> HTTP header before processing the request.
     *
     * <p>This is a strange implementation, because intuitively, you would add
     * the header after the content has been produced (in other words, after you
     * had already called {@link FilterChain#doFilter FilterChain.doFilter}.  However,
     * the Servlet 2.3 spec (proposed final draft) states (albeitly in an off-handed
     * fashion) that you can only "examine" the response headers after the 
     * <code>doFilter</code> call.  Evidently this means that you cannot change the 
     * headers after the <code>doFilter</code>.  If you call <code>setHeader</code>
     * nothing happens. </p>
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
        ((HttpServletResponse)response).setHeader( "Refresh", this.seconds );
        chain.doFilter( request, response );
    }


    public void init( FilterConfig config ) {
        this.filterConfig = config;

        //read the seconds value from the config or use the default if not found
        String seconds = this.filterConfig.getInitParameter( "seconds" );
        if( seconds != null ); {
            this.seconds = seconds;
        }
    }


    public void destroy() {}

}
