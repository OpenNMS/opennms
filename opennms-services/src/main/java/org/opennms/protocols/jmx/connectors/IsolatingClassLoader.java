//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact: 
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.protocols.jmx.connectors;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;


/**
 * An extension of the URLClassLoader that ensures it loads specified
 * packages rather letting the parent do it. The result is that classes
 * loaded from these packages are isolated from other classloaders.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public class IsolatingClassLoader extends URLClassLoader {
    
    private String m_name;
    
    /** Array of prefixes that identifies packages or classes to isolate. **/
    private String[] m_isolatedPrefixes;
    
    /** Set of class names that identifies classes to isolate. **/
    private Set<String> m_isolatedClassNames = new HashSet<String>();
    
    /**
     * <p>Constructor for IsolatingClassLoader.</p>
     *
     * @param classpath Where to find classes.
     * @param isolated Array of fully qualified class names, or fully
     * qualified prefixes ending in "*", that identify the packages or
     * classes to isolate.
     * @param augmentClassPath true => Add the URL's of the current
     * thread context class loader to <code>classpath</code>.
     * @throws org.opennms.protocols.jmx.connectors.IsolatingClassLoader.InvalidContextClassLoaderException If augmentClassPath
     * is true and the current thread context class loader is not a
     * <code>URLClassLoader</code>.
     * @param name a {@link java.lang.String} object.
     */
    public IsolatingClassLoader(String name, URL[] classpath, String[] isolated, boolean augmentClassPath) throws InvalidContextClassLoaderException {
        
        super(classpath);
        init(name, isolated, augmentClassPath);
    }
    
    /**
     * <p>Constructor for IsolatingClassLoader.</p>
     *
     * @param classpath Where to find classes.
     * @param isolated Array of fully qualified class names, or fully
     * qualified prefixes ending in "*", that identify the packages or
     * classes to isolate.
     * @param augmentClassPath true => Add the URL's of the current
     * thread context class loader to <code>classpath</code>.
     * @throws org.opennms.protocols.jmx.connectors.IsolatingClassLoader.InvalidContextClassLoaderException If augmentClassPath
     * is true and the current thread context class loader is not a
     * <code>URLClassLoader</code>.
     * @param name a {@link java.lang.String} object.
     * @param parent a {@link java.lang.ClassLoader} object.
     */
    public IsolatingClassLoader(String name, URL[] classpath, ClassLoader parent, String[] isolated, boolean augmentClassPath)   throws InvalidContextClassLoaderException {
        
        super(classpath, parent);
        init(name, isolated, augmentClassPath);
    }
    
    private void init(String name, String[] isolated, boolean augmentClassPath) throws InvalidContextClassLoaderException {
        
        m_name = name;
        
        final Set<String> prefixes = new HashSet<String>();
        
        for (int i=0; i<isolated.length; i++) {
            final int index = isolated[i].indexOf('*');
            
            if (index >= 0) {
                prefixes.add(isolated[i].substring(0, index));
            }
            else {
                m_isolatedClassNames.add(isolated[i]);
            }
        }
        
        m_isolatedPrefixes = (String[])prefixes.toArray(new String[0]);
        
        if (augmentClassPath) {
            final ClassLoader callerClassLoader = Thread.currentThread().getContextClassLoader();
            
            if (callerClassLoader instanceof URLClassLoader) {
                final URL[] newURLs = ((URLClassLoader)callerClassLoader).getURLs();
                
                for (int i=0; i<newURLs.length; i++) {
                    addURL(newURLs[i]);
                }
            }
            else {
                throw new InvalidContextClassLoaderException("Caller classloader is not a URLClassLoader, " + "can't automatically augument classpath." + "Its a " + callerClassLoader.getClass());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * Override to only check parent ClassLoader if the class name
     * doesn't match our list of isolated classes.
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        
        boolean isolated = m_isolatedClassNames.contains(name);
        
        if (!isolated) {
            for (int i=0; i<m_isolatedPrefixes.length; i++) {
                
                if (name.startsWith(m_isolatedPrefixes[i])) {
                    isolated = true;
                    break;
                }
            }
        }
        
        if (isolated) {
            Class c = findLoadedClass(name);
            
            if (c == null) {
                c = findClass(name);
            }
            
            if (resolve) {
                resolveClass(c);
            }
            
            return c;
            
        }
        
        return super.loadClass(name, resolve);
    }
    
    public static class InvalidContextClassLoaderException extends Exception {
        
        /**
         * 
         */
        private static final long serialVersionUID = -82741827583768184L;

        public InvalidContextClassLoaderException(String message) {
            
            super(message);
        }
    }
}

