//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//

package org.opennms.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.opennms.web.category.RTCPostSubscriber;


/**
 * Initializes our internal servlet systems at servlet container startup, and
 * destroys any pool resources at servlet container shutdown.
 *
 * This listener is specified in the web.xml to listen to <code>ServletContext</code>
 * lifecyle events.  On startup it calls ServletInitializer.init
 * and initializes the UserFactory, GroupFactory.
 * On shutdown it calls ServletInitializer.destroy.
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
