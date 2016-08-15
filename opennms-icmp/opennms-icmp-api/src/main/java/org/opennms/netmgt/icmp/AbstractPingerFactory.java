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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PingerFactory class.</p>
 *
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="mailto:seth@opennms.org">Seth Leger</A>
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 */
public abstract class AbstractPingerFactory implements PingerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPingerFactory.class);

    private volatile Pinger[][] m_pingers = new Pinger[MAX_DSCP][2];

    public abstract Class<? extends Pinger> getPingerClass();

    protected Pinger getPinger(final int tc, final int isFrag) {
        // because we'll basically never set an instance that's already created
        // we can assume it's OK to not worry about concurrency for a hit
        if (m_pingers[tc] != null && m_pingers[tc][isFrag] != null) {
            return m_pingers[tc][isFrag];
        }
        return null;
    }

    public Pinger getInstance() {
        return getInstance(0, true);
    }

    public Pinger getInstance(final int tc, final boolean allowFragmentation) {
        final int isFrag = allowFragmentation? FRAG_TRUE : FRAG_FALSE;

        final Pinger existing = getPinger(tc, isFrag);
        if (existing != null) {
            return existing;
        }

        // OTHERWISE, we have to lock to write
        synchronized(m_pingers) {
            final Class<? extends Pinger> clazz;

            try {
                clazz = getPingerClass();
            } catch (final RuntimeException e) {
                IllegalArgumentException ex = new IllegalArgumentException("Unable to find class named " + System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jni6.Jni6Pinger"), e);
                LOG.error(ex.getLocalizedMessage(), ex);
                throw ex;
            }

            try {
                final Pinger pinger = clazz.newInstance();
                pinger.setTrafficClass(tc);
                m_pingers[tc][isFrag] = pinger;
            } catch (final InstantiationException e) {
                IllegalArgumentException ex = new IllegalArgumentException("Error trying to create pinger of type " + clazz, e);
                LOG.error(ex.getLocalizedMessage(), ex);
                throw ex;
            } catch (final IllegalAccessException e) {
                IllegalArgumentException ex = new IllegalArgumentException("Unable to create pinger of type " + clazz + ".  It does not appear to have a public constructor", e);
                LOG.error(ex.getLocalizedMessage(), ex);
                throw ex;
            } catch (final Throwable e) {
                IllegalArgumentException ex = new IllegalArgumentException("Unexpected exception thrown while trying to create pinger of type " + clazz, e);
                LOG.error(ex.getLocalizedMessage(), ex);
                throw ex;
            }
            return m_pingers[tc][isFrag];
        }
    }

    public void setInstance(final int tc, final boolean allowFragmentation, final Pinger pinger) {
        synchronized(m_pingers) {
            m_pingers[tc][allowFragmentation? FRAG_TRUE : FRAG_FALSE] = pinger;
        }
    }

    public void reset() {
        synchronized(m_pingers) {
            m_pingers = new Pinger[MAX_DSCP][2];
        }
    }
}
