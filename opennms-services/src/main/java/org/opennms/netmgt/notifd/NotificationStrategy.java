/*
 * Modifications:
 *
 * 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
 * 2004 Sep 08: This file created.
 * 
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 */
package org.opennms.netmgt.notifd;

import java.util.List;

import org.opennms.core.utils.Argument;

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

    public int send(List<Argument> arguments);

}
