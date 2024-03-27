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
package org.opennms.core.utils.url;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * <p>GenericURLStreamHandler class.</p>
 * Customized URL stream handler creates dynamically created URL connections from customized URL protocols.
 *
 * @author <a href="mailto:christian.pape@informatik.hs-fulda.de">Christian Pape</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public class GenericURLStreamHandler extends URLStreamHandler {
    private Class<? extends URLConnection> urlConnectionClass;
    private int defaultPort = -1;

    /**
     * <p>GenericURLStreamHandler</p>
     * Create URL stream handler with given class and customized default port.
     *
     * @param urlConnectionClass full qualified classname as {@link java.lang.String} object.
     * @param defaultPort        default port as {@link java.lang.Integer} object.
     */
    public GenericURLStreamHandler(Class<? extends URLConnection> urlConnectionClass, int defaultPort) {
        this.urlConnectionClass = urlConnectionClass;
        this.defaultPort = defaultPort;
    }

    /**
     * <p>GenericURLStreamHandler</p>
     * Create URL stream handler with given class and default port -1.
     *
     * @param urlConnectionClass full qualified classname as {@link java.lang.String} object.
     */
    public GenericURLStreamHandler(Class<? extends URLConnection> urlConnectionClass) {
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
            Constructor<? extends URLConnection> constructor = urlConnectionClass
                    .getConstructor(new Class[]{URL.class});
            urlConnection = constructor.newInstance(u);
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

}
