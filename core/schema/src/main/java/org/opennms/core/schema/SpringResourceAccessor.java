/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.schema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import liquibase.resource.ResourceAccessor;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class SpringResourceAccessor implements ResourceAccessor {
    private ResourceLoader m_resourceLoader = new DefaultResourceLoader();

    public SpringResourceAccessor() {
        
    }
    
    /** {@inheritDoc} */
    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
    	final Resource resource = getResource(file);
        return resource.getInputStream();
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
    	final Vector<URL> tmp = new Vector<URL>();
        tmp.add(getResource(packageName).getURL());
        System.err.println("getResources(" + packageName + ") returning: " + tmp);
        return tmp.elements();
    }

    /**
     * <p>getResource</p>
     *
     * @param file a {@link java.lang.String} object.
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getResource(final String file) {
    	final File f = new File(file);
        if (f.exists()) {
            return getResourceLoader().getResource(file);
        } else {
            return getResourceLoader().getResource(adjustClasspath(file));
        }
    }

    private String adjustClasspath(final String file) {
        return !isClasspathPrefixPresent(file) ? ResourceLoader.CLASSPATH_URL_PREFIX + file : file;
    }

    /**
     * <p>isClasspathPrefixPresent</p>
     *
     * @param file a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isClasspathPrefixPresent(final String file) {
        return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
    }

    /**
     * <p>toClassLoader</p>
     *
     * @return a {@link java.lang.ClassLoader} object.
     */
    @Override
    public ClassLoader toClassLoader() {
        return getResourceLoader().getClassLoader();
    }

    /**
     * <p>setResourceLoader</p>
     *
     * @param resourceLoader a {@link org.springframework.core.io.ResourceLoader} object.
     */
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        m_resourceLoader = resourceLoader;
    }

    /**
     * <p>getResourceLoader</p>
     *
     * @return a {@link org.springframework.core.io.ResourceLoader} object.
     */
    public ResourceLoader getResourceLoader() {
        return m_resourceLoader;
    }
}
