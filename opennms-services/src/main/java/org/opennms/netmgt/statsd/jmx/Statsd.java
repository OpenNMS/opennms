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
package org.opennms.netmgt.statsd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

/**
 * <p>Statsd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Statsd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.statsd.Statsd> implements StatsdMBean {
    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.statsd.Statsd.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "statisticsDaemonContext";       
    }

    /** {@inheritDoc} */
    @Override
    public long getReportsStarted() {
        return getDaemon().getReportsStarted();
    }

    /** {@inheritDoc} */
    @Override
    public long getReportsCompleted() {
        return getDaemon().getReportsCompleted();
    }

    /** {@inheritDoc} */
    @Override
    public long getReportsPersisted() {
        return getDaemon().getReportsPersisted();
    }

    /** {@inheritDoc} */
    @Override
    public long getReportRunTime() {
        return getDaemon().getReportRunTime();
    }

}
