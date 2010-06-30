/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

/**
 * <p>DetectorMonitor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface DetectorMonitor {

    /**
     * <p>start</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void start(ServiceDetector detector, String format, Object... args);

    /**
     * <p>stopped</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void stopped(ServiceDetector detector, String format, Object... args);

    /**
     * <p>attempt</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param attempt a int.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void attempt(ServiceDetector detector, int attempt, String format, Object... args);

    /**
     * <p>info</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param e a {@link java.lang.Exception} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void info(ServiceDetector detector, Exception e, String format, Object... args);

    /**
     * <p>error</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param t a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void error(ServiceDetector detector, Throwable t, String format, Object... args);

    /**
     * <p>success</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void success(ServiceDetector detector, String format, Object... args);

    /**
     * <p>failure</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.ServiceDetector} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public void failure(ServiceDetector detector, String format, Object... args);
    
}
