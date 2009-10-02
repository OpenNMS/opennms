//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Eliminate deprecated method. - dj@opennms.org
// 2007 May 12: Dedeplicate and use Java 5 generics. - dj@opennms.org
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
//      http://www.opennms.com/
//
//
// Tab Size = 8
//

package org.opennms.core.utils;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * This class is designed to work with log4j based on threads and not class
 * names. This allows all the classes invoked by a thread to log their messages
 * to the same location. This is particularly useful when messages from share
 * common code should be associated with a higher level <EM>service</EM> or
 * <EM>application</EM>.
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ThreadCategory extends Category {
    private static final String DEFAULT_CATEGORY = "UNCATEGORIZED";
    
    /**
     * This thread local variable is used to store the category that threads
     * (and their children) should use to log information. If a thread has a set
     * category and then starts a new thread, the new thread will inherit the
     * category of the parent thread.
     */
    private static InheritableThreadLocal<String> s_threadCategory = new InheritableThreadLocal<String>();

    /**
     * This constructor created a new Category instance and sets its name. It is
     * intended to be used by sub-classes only. You should not create categories
     * directly.
     * 
     * @param name
     *            The name of the category
     * 
     */
    protected ThreadCategory(String name) {
        super(name);
    }

    /**
     * This method is used to get the category instance associated with the
     * thread. If the category for the thread has not been set then the passed
     * class is used to find the appropriate category. If a category is found
     * for the thread group then it is returned to the caller.
     * 
     * @param c
     *            The class used to find the category if it was not set.
     * 
     * @return The instance for the thread.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static Logger getInstance(Class<?> c) {
        String prefix = getPrefix();

        if ((prefix != null) && !prefix.equals("")) {
            return Logger.getLogger(prefix + "." + c.getName());
        } else {
            return Logger.getLogger(c.getName());
        }
    }

    /**
     * This method is used to get the category instance associated with the
     * thread. If the category for the thread has not been set then the passed
     * name is used to find the appropriate category. If a category is found for
     * the thread group then it is returned to the caller.
     * 
     * @param cname
     *            The name used to find the category if it was not set.
     * 
     * @return The instance for the thread.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static Logger getInstance(String cname) {
        String prefix = getPrefix();

        if ((prefix != null) && !prefix.equals("")) {
            return Logger.getLogger(prefix + "." + cname);
        } else {
            return Logger.getLogger(cname);
        }
    }

    /**
     * This method is used to get the category instance associated with the
     * thread. If the instance has not been set then a default category is
     * returned to the caller.
     * 
     * @return The instance for the thread, null if it is not set.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static Logger getInstance() {
        String prefix = getPrefix();
        
        if (prefix != null) {
            return Logger.getLogger(prefix);
        } else {
            /*
             * Use the default category anywhere that ThreadCategory
             * is instantiated without a prefix, classname, or user-
             * specified string.
             */
            return Logger.getLogger(DEFAULT_CATEGORY);
        }
    }

    /**
     * This method is used to set a prefix for the category name that is used
     * for all category instances in this thread. This is used to insure that
     * all messages from a particular thread end up in the same log file,
     * regardless of the package or class name of the class that generated the
     * log message. Please restrict the usage of this function to only the
     * highest level threads.
     */
    public static void setPrefix(String prefix) {
        s_threadCategory.set(prefix);
    }

    /**
     * This is used to retreive the current prefix as it has been inhereited by
     * the calling thread. This is needed by many dyanmic threading classes like
     * the <code>RunnableConsumerThreadPool</code> to ensure that all the
     * internal threads run in the same category.
     * 
     * @return The prefix string as inherieted by the calling thread.
     */
    public static String getPrefix() {
        return s_threadCategory.get();
    }
}
