/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.url;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * <p>GenericURLStreamHandler class.</p>
 * <p/>
 * Customized URL stream handler creates dynamically created URL connections from customized URL protocols.
 *
 * @author <a href="mailto:christian.pape@informatik.hs-fulda.de">Christian Pape</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class GenericURLStreamHandler extends URLStreamHandler {
    private Class<URLConnection> urlConnectionClass;
    private int defaultPort = -1;

    /**
     * <p>GenericURLStreamHandler</p>
     * <p/>
     * Create URL stream handler with given class and customized default port.
     *
     * @param urlConnectionClass full qualified classname as {@java.lang.String} object.
     * @param defaultPort        default port as {@java.lang.int} object.
     */
    public GenericURLStreamHandler(Class<URLConnection> urlConnectionClass, int defaultPort) {
        this.urlConnectionClass = urlConnectionClass;
        this.defaultPort = defaultPort;
    }

    /**
     * <p>GenericURLStreamHandler</p>
     * <p/>
     * Create URL stream handler with given class and default port -1.
     *
     * @param urlConnectionClass full qualified classname as {@java.lang.String} object.
     */
    public GenericURLStreamHandler(Class<URLConnection> urlConnectionClass) {
        this(urlConnectionClass, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getDefaultPort() {
        return defaultPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        URLConnection urlConnection = null;
        try {
            Constructor constructor = urlConnectionClass
                    .getConstructor(new Class[]{URL.class});
            urlConnection = (URLConnection) constructor
                    .newInstance(new Object[]{u});
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return urlConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
        super.parseURL(u, spec, start, limit);
    }
}
