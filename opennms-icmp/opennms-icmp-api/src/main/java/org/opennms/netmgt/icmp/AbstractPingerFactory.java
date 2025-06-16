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
package org.opennms.netmgt.icmp;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * <p>PingerFactory class.</p>
 *
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="mailto:seth@opennms.org">Seth Leger</A>
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 */
public abstract class AbstractPingerFactory implements PingerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPingerFactory.class);

    protected static final Cache<Integer, Pinger> m_pingers = CacheBuilder.newBuilder().build();

    public abstract Class<? extends Pinger> getPingerClass();

    public Pinger getInstance() {
        return getInstance(0, true);
    }

    public Pinger getInstance(final int tc, final boolean allowFragmentation) {
        final int isFrag = allowFragmentation? FRAG_TRUE : FRAG_FALSE;
        final Class<? extends Pinger> clazz;

        try {
            clazz = getPingerClass();
        } catch (final RuntimeException e) {
            IllegalArgumentException ex = new IllegalArgumentException("Unable to find class named " + System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jni6.Jni6Pinger"), e);
            LOG.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }

        try {
            return m_pingers.get((tc + 1) * isFrag, new Callable<Pinger>() {
                @Override
                public Pinger call() throws Exception {
                    final Pinger pinger = clazz.newInstance();
                    pinger.setTrafficClass(tc);
                    return pinger;
                }
            });
        } catch (final Throwable e) {
            final IllegalArgumentException ex;
            if (e.getCause() instanceof InstantiationException) {
                ex = new IllegalArgumentException("Error trying to create pinger of type " + clazz, e.getCause());
            } else if (e.getCause() instanceof IllegalAccessException) {
                ex = new IllegalArgumentException("Unable to create pinger of type " + clazz + ".  It does not appear to have a public constructor", e);
            } else {
                ex = new IllegalArgumentException("Unexpected exception thrown while trying to create pinger of type " + clazz, e);
            }
            LOG.error(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    public void setInstance(final int tc, final boolean allowFragmentation, final Pinger pinger) {
        final int isFrag = allowFragmentation? FRAG_TRUE : FRAG_FALSE;
        m_pingers.put((tc + 1) * isFrag, pinger);
    }

    public void reset() {
        m_pingers.invalidateAll();
        m_pingers.cleanUp();
    }
}
