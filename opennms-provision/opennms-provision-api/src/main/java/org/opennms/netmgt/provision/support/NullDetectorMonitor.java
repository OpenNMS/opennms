/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;

/**
 * <p>NullDetectorMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NullDetectorMonitor implements DetectorMonitor{

    /**
     * <p>attempt</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param attempt a int.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void attempt(final ServiceDetector detector, final int attempt, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

    /**
     * <p>error</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param t a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void error(final ServiceDetector detector, final Throwable t, final String format, final Object... args) {
        LogUtils.infof(this, t, format, args);
    }

    /**
     * <p>failure</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void failure(final ServiceDetector detector, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

    /**
     * <p>info</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param e a {@link java.lang.Exception} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void info(final ServiceDetector detector, final Exception e, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

    /**
     * <p>start</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void start(final ServiceDetector detector, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

    /**
     * <p>stopped</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void stopped(final ServiceDetector detector, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

    /**
     * <p>success</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void success(final ServiceDetector detector, final String format, final Object... args) {
        LogUtils.infof(this, format, args);
    }

}
