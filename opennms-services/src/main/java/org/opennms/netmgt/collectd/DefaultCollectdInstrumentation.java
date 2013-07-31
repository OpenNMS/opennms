/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultCollectdInstrumentation implements CollectdInstrumentation {
    public static final Logger LOG = LoggerFactory.getLogger(DefaultCollectdInstrumentation.class);

    @Override
    public void beginScheduleExistingInterfaces() {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleExistingInterfaces: begin");
            }
        });
    }

    @Override
    public void endScheduleExistingInterfaces() {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleExistingInterfaces: end");
            }
        });
    }

    @Override
    public void beginScheduleInterfacesWithService(final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleInterfacesWithService: begin: {}", svcName);
            }
        });
    }

    @Override
    public void endScheduleInterfacesWithService(final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleInterfacesWithService: end: {}", svcName);
            }
        });
    }

    @Override
    public void beginFindInterfacesWithService(final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleFindInterfacesWithService: begin: {}", svcName);
            }
        });
    }

    @Override
    public void endFindInterfacesWithService(final String svcName, final int count) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleFindInterfacesWithService: end: {}. found {} interfaces.", svcName, count);
            }
        });
    }

    @Override
    public void beginCollectingServiceData(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: collectData: begin: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endCollectingServiceData(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: collectData: end: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void beginCollectorCollect(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: begin:{}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endCollectorCollect(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: end:{}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void beginCollectorRelease(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.release: begin: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endCollectorRelease(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.release: end: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void beginPersistingServiceData(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: persistDataQueueing: begin: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endPersistingServiceData(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: persistDataQueueing: end: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void beginCollectorInitialize(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.initialize: begin: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endCollectorInitialize(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.initialize: end: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void beginScheduleInterface(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleInterfaceWithService: begin: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void endScheduleInterface(final int nodeId, final String ipAddress, final String svcName) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("scheduleInterfaceWithService: end: {}/{}/{}", nodeId, ipAddress, svcName);
            }
        });
    }

    @Override
    public void reportCollectionException(final int nodeId, final String ipAddress, final String svcName, final CollectionException e) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.debug("collector.collect: error: {}/{}/{}", nodeId, ipAddress, svcName, e);
            }
        });
    }

}
