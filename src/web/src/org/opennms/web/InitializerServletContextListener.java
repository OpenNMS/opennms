package org.opennms.web;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Properties;
import javax.servlet.*;
import org.opennms.core.resource.Vault;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.category.RTCPostSubscriber;


/**
 * Initializes our internal servlet systems at servlet container startup, and
 * destroys any pool resources at servlet container shutdown.
 *
 * <p>This listener is specified in the web.xml to listen to <code>ServletContext</code>
 * lifecyle events.  On startup it calls {@link ServletInitializer#init ServletInitializer.init}
 * and initializes the {@link UserFactory UserFactory}, {@link GroupFactory GroupFactory}.
 * On shutdown it calls 
 * {@link ServletInitializer#destroy ServletInitializer.destroy}.</p>
 *         
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class InitializerServletContextListener extends Object implements ServletContextListener
{
    public void contextInitialized( ServletContextEvent event ) {
        ServletContext context = event.getServletContext();
        
        try {
            //initialize the scarce resource policies (db connections) and common configuration 
            ServletInitializer.init( context );
            context.log( "[InitializerServletContextListener] Initialized servlet systems successfully" );
        }
        catch( ServletException e ) {
            context.log( "[InitializerServletContextListener] Error while initializing servlet systems", e ); 
        }
        catch( Exception e ) {
            context.log( "[InitializerServletContextListener] Error while initializing user, group, or view factory", e ); 
        }                
        
        try {
            RTCPostSubscriber.subscribeAll("WebConsoleView");
            context.log( "[InitializerServletContextListener] Initialized RTC POST subscription event sent successfully" );
        }
        catch( Exception e ) {
            context.log( "[InitializerServletContextListener] Error subscribing to RTC POSTs", e ); 
        }        
    }


    public void contextDestroyed( ServletContextEvent event ) {
        try {            
            //let the scarce resource policies release any shared resouces (db connections)  
            ServletInitializer.destroy( event.getServletContext() );

            //report success
            event.getServletContext().log( "[InitializerServletContextListener] Destroyed servlet systems successfully" );
        }
        catch( ServletException e ) {
            event.getServletContext().log( "[InitializerServletContextListener] Error while destroying servlet systems", e ); 
        }
    }
}
