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
package org.opennms.netmgt.notifd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Notifd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Notifd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.notifd.Notifd> implements NotifdMBean {

    private static final Logger LOG = LoggerFactory.getLogger(Notifd.class);

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.notifd.Notifd.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "notifdContext";
    }

    @Override
    /** {@inheritDoc} */
    public long getNotificationTasksQueued() {
        return getDaemon().getNotificationManager().getNotificationTasksQueued();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesAttempted() {
        return getDaemon().getNotificationManager().getBinaryNoticesAttempted();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesAttempted() {
        return getDaemon().getNotificationManager().getJavaNoticesAttempted();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesSucceeded() {
        return getDaemon().getNotificationManager().getBinaryNoticesSucceeded();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesSucceeded() {
        return getDaemon().getNotificationManager().getJavaNoticesSucceeded();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesFailed() {
        return getDaemon().getNotificationManager().getBinaryNoticesFailed();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesFailed() {
        return getDaemon().getNotificationManager().getJavaNoticesFailed();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesInterrupted() {
        return getDaemon().getNotificationManager().getBinaryNoticesInterrupted();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesInterrupted() {
        return getDaemon().getNotificationManager().getJavaNoticesInterrupted();
    }

    @Override
    /** {@inheritDoc} */
    public long getUnknownNoticesInterrupted() {
        return getDaemon().getNotificationManager().getUnknownNoticesInterrupted();
    }
}
