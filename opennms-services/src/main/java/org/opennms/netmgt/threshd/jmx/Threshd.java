/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd.jmx;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Threshd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Threshd extends AbstractServiceDaemon implements ThreshdMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(Threshd.class);
    
    /**
     * <p>Constructor for Threshd.</p>
     */
    public Threshd() {
        super(NAME);
    }

    /**
     * Log4j category
     */
    private final static String NAME = "threshd";

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        // Load threshd configuration file
        //
        try {
            ThreshdConfigFactory.reload();
            ThresholdingConfigFactory.reload();
        } catch (MarshalException ex) {
            LOG.error("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            LOG.error("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            LOG.error("start: Failed to load threshd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        
        // Load up the configuration for the scheduled outages.
        //
        try {
            PollOutagesConfigFactory.reload();
        } catch (MarshalException ex) {
            LOG.error("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            LOG.error("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            LOG.error("start: Failed to load poll-outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }


        
        getInstance().setThreshdConfig(ThreshdConfigFactory.getInstance());

        getInstance().init();
    }

    private org.opennms.netmgt.threshd.Threshd getInstance() {
        return org.opennms.netmgt.threshd.Threshd.getInstance();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        getInstance().start();
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        getInstance().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getInstance().getStatus();
    }
}
