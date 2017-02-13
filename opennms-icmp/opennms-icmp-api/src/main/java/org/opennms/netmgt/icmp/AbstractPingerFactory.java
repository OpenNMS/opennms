/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
