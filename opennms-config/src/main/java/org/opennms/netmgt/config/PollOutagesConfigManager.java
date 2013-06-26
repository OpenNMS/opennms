/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.poller.Interface;
import org.opennms.netmgt.config.poller.Node;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.config.poller.Time;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * Represents a PollOutagesConfigManager
 *
 * @author brozow
 */
abstract public class PollOutagesConfigManager extends AbstractJaxbConfigDao<Outages, Outages> implements PollOutagesConfig {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    public PollOutagesConfigManager() {
        super(Outages.class, "poll outage configuration");
    }

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws DataAccessException {
        /**
         * It sucks to duplicate this first test from AbstractCastorConfigDao,
         * but we need to do so to ensure we don't get an NPE while initializing
         * programmaticStoreConfigResource (if needed).
         */
        Assert.state(getConfigResource() != null, "property configResource must be set and be non-null");
        
        super.afterPropertiesSet();
    }

    @Override
    protected Outages translateConfig(final Outages outages) {
        return outages;
    }
    

    /**
     * <p>getConfig</p>
     *
     * @return Returns the config.
     */
    protected Outages getConfig() {
        getReadLock().lock();
        try {
            return getContainer().getObject();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the outages configured.
     *
     * @return the outages configured
     */
    public Outage[] getOutages() {
        getReadLock().lock();
        try {
            return getConfig().getOutage();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the specified outage, null if not found
     */
    public Outage getOutage(final String name) {
        getReadLock().lock();
        try {
            for (final Outage out : getConfig().getOutageCollection()) {
                if (BasicScheduleUtils.getBasicOutageSchedule(out).getName().equals(name)) {
                    return out;
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

    /**
     * Return the type for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the type for the specified outage, null if not found
     */
    public String getOutageType(final String name) {
        final Outage out = getOutage(name);
        if (out == null) return null;
        return out.getType();
    }

    /**
     * Return the outage times for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the outage times for the specified outage, null if not found
     */
    public Time[] getOutageTimes(final String name) {
        final Outage out = getOutage(name);
        if (out == null) return null;
        return out.getTime();
    }

    /**
     * Return the interfaces for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the interfaces for the specified outage, null if not found
     */
    public Interface[] getInterfaces(final String name) {
        final Outage out = getOutage(name);
        if (out == null) return null;
        return out.getInterface();
    }

    /**
     * {@inheritDoc}
     *
     * Return if interfaces is part of specified outage.
     */
    @Override
    public boolean isInterfaceInOutage(final String linterface, final String outName) {
        final Outage out = getOutage(outName);
        if (out == null) return false;
        return isInterfaceInOutage(linterface, out);
    }

    /**
     * Return if interfaces is part of specified outage.
     *
     * @param linterface
     *            the interface to be looked up
     * @param getOutageSchedule(out)
     *            the outage
     * @return the interface is part of the specified outage
     */
    public boolean isInterfaceInOutage(final String linterface, final Outage out) {
        if (out == null) return false;

        for (final Interface ointerface : out.getInterfaceCollection()) {
            if (ointerface.getAddress().equals("match-any") || ointerface.getAddress().equals(linterface)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Return if time is part of specified outage.
     */
    public boolean isTimeInOutage(final Calendar cal, final String outName) {
        final Outage out = getOutage(outName);
        if (out == null) return false;

        return isTimeInOutage(cal, out);
    }

    /**
     * {@inheritDoc}
     *
     * Return if time is part of specified outage.
     */
    @Override
    public boolean isTimeInOutage(final long time, final String outName) {
        final Outage out = getOutage(outName);
        if (out == null) return false;

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return isTimeInOutage(cal, out);
    }

    /**
     * Return if time is part of specified outage.
     *
     * @param cal
     *            the calendar to lookup
     * @param getOutageSchedule(outage)
     *            the outage
     * @return true if time is in outage
     */
    public boolean isTimeInOutage(final Calendar cal, final Outage outage) {
        return BasicScheduleUtils.isTimeInSchedule(cal, BasicScheduleUtils.getBasicOutageSchedule(outage));

    }

    /**
     * {@inheritDoc}
     *
     * Return if current time is part of specified outage.
     */
    @Override
    public boolean isCurTimeInOutage(final String outName) {
        return isTimeInOutage(new GregorianCalendar(), outName);
    }

    /**
     * Return if current time is part of specified outage.
     *
     * @param getOutageSchedule(out)
     *            the outage
     * @return true if current time is in outage
     */
    public boolean isCurTimeInOutage(final Outage out) {
        return isTimeInOutage(new GregorianCalendar(), out);
    }

    /**
     * <p>addOutage</p>
     *
     * @param getOutageSchedule(newOutage) a {@link org.opennms.netmgt.config.poller.Outage} object.
     */
    public void addOutage(final Outage newOutage) {
        getConfig().addOutage(newOutage);
    }

    /**
     * <p>removeOutage</p>
     *
     * @param outageName a {@link java.lang.String} object.
     */
    public void removeOutage(final String outageName) {
        getConfig().removeOutage(getOutage(outageName));
    }

    /**
     * <p>removeOutage</p>
     *
     * @param getOutageSchedule(outageToRemove) a {@link org.opennms.netmgt.config.poller.Outage} object.
     */
    public void removeOutage(final Outage outageToRemove) {
        getConfig().removeOutage(outageToRemove);
    }

    /**
     * <p>replaceOutage</p>
     *
     * @param getOutageSchedule(oldOutage) a {@link org.opennms.netmgt.config.poller.Outage} object.
     * @param getOutageSchedule(newOutage) a {@link org.opennms.netmgt.config.poller.Outage} object.
     */
    public void replaceOutage(final Outage oldOutage, final Outage newOutage) {
        int count = getConfig().getOutageCount();
        for (int i = 0; i < count; i++) {
            if (getConfig().getOutage(i).equals(oldOutage)) {
                getConfig().setOutage(i, newOutage);
                return;
            }
        }
    }

    /*
     * <p>Return the nodes for specified outage</p>
     * 
     * @param name the outage that is to be looked up
     * 
     * @return the nodes for the specified outage, null if not found
     */
    /**
     * <p>getNodeIds</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.poller.Node} objects.
     */
    public Node[] getNodeIds(final String name) {
        final Outage out = getOutage(name);
        if (BasicScheduleUtils.getBasicOutageSchedule(out) == null) return null;
        return out.getNode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Return if nodeid is part of specified outage
     * </p>
     */
    @Override
    public boolean isNodeIdInOutage(final long lnodeid, final String outName) {
        final Outage out = getOutage(outName);
        if (out == null) return false;
        return isNodeIdInOutage(lnodeid, out);
    }

    /**
     * <p>getEndOfOutage</p>
     *
     * @param outName a {@link java.lang.String} object.
     * @return a {@link java.util.Calendar} object.
     */
    public Calendar getEndOfOutage(final String outName) {
        final Outage out = getOutage(outName);
        if (out == null) return null;
        return getEndOfOutage(out);
    }

    /**
     * Return a calendar representing the end time of this outage, assuming it's
     * currently active (i.e. right now is within one of the time periods)
     *
     * FIXME: This code is almost identical to isTimeInOutage... We need to fix
     * it
     *
     * @param getOutageSchedule(out) a {@link org.opennms.netmgt.config.poller.Outage} object.
     * @return a {@link java.util.Calendar} object.
     */
    public static Calendar getEndOfOutage(final Outage out) {
        // FIXME: We need one that takes the time as a parm.  This makes it more testable
        return BasicScheduleUtils.getEndOfSchedule(BasicScheduleUtils.getBasicOutageSchedule(out));
    }

	/**
     * <p>
     * Return if nodeid is part of specified outage
     * </p>
     *
     * @param lnodeid
     *            the nodeid to be looked up
     * @return the node iis part of the specified outage
     * @param getOutageSchedule(out) a {@link org.opennms.netmgt.config.poller.Outage} object.
     */
    public boolean isNodeIdInOutage(final long lnodeid, final Outage out) {
        if (out == null) return false;

        for (final Node onode : out.getNodeCollection()) {
            if (onode.getId() == lnodeid) {
                return true;
            }
        }

        return false;
    }
}
