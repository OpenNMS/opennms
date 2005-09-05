/*
 * Created on Sep 8, 2004
 * 
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.notifd;

import java.util.List;

/**
 * Implement this interface as a Java notification "plug-in" for use with the
 * notficationCommands.xml file. Build a class using this interface, and in the
 * xml file set binary=false, and specify the class in the execute tag.
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface NotificationStrategy {

    public int send(List arguments);

}
