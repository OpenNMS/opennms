/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.jmx.impl.connection.connectors;

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
 */
class IsolatingClassLoader extends URLClassLoader {
    
    /** Array of prefixes that identifies packages or classes to isolate. **/
    private String[] m_isolatedPrefixes;
    
    /** Set of class names that identifies classes to isolate. **/
    private final Set<String> m_isolatedClassNames = new HashSet<>();

    /**
     * <p>Constructor for IsolatingClassLoader.</p>
     *
     * @param classpath Where to find classes.
     * @param isolated Array of fully qualified class names, or fully
     * qualified prefixes ending in "*", that identify the packages or
     * classes to isolate.
     * @param augmentClassPath true => Add the URL's of the current
     * thread context class loader to <code>classpath</code>.
     * @throws InvalidContextClassLoaderException If augmentClassPath is true and the current thread context class loader is not a {@link java.net.URLClassLoader}.
     * @param name a {@link String} object.
     * @param parent a {@link ClassLoader} object.
     */
    public IsolatingClassLoader(String name, URL[] classpath, ClassLoader parent, String[] isolated, boolean augmentClassPath)   throws InvalidContextClassLoaderException {
        super(classpath, parent);
        init(name, isolated, augmentClassPath);
    }
    
    private void init(String name, String[] isolated, boolean augmentClassPath) throws InvalidContextClassLoaderException {
        
        final Set<String> prefixes = new HashSet<>();
        
        for (String element : isolated) {
            final int index = element.indexOf('*');
            
            if (index >= 0) {
                prefixes.add(element.substring(0, index));
            }
            else {
                m_isolatedClassNames.add(element);
            }
        }
        
        m_isolatedPrefixes = prefixes.toArray(new String[0]);
        
        if (augmentClassPath) {
            final ClassLoader callerClassLoader = Thread.currentThread().getContextClassLoader();
            
            if (callerClassLoader instanceof URLClassLoader) {
                final URL[] newURLs = ((URLClassLoader)callerClassLoader).getURLs();
                
                for (URL newURL : newURLs) {
                    addURL(newURL);
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
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        
        boolean isolated = m_isolatedClassNames.contains(name);
        
        if (!isolated) {
            for (String prefix : m_isolatedPrefixes) {
                
                if (name.startsWith(prefix)) {
                    isolated = true;
                    break;
                }
            }
        }
        
        if (isolated) {
            Class<?> c = findLoadedClass(name);
            
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
        
        private static final long serialVersionUID = -82741827583768184L;

        public InvalidContextClassLoaderException(String message) {
            
            super(message);
        }
    }
}
