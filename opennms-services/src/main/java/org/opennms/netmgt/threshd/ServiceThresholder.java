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

package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.netmgt.model.events.EventProxy;

/**
 * <P>
 * The Thresholder class...
 * </P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public interface ServiceThresholder {
    /**
     * Status of the thresholder object.
     */
    public static final int THRESHOLDING_UNKNOWN = 0;

    /** Constant <code>THRESHOLDING_SUCCEEDED=1</code> */
    public static final int THRESHOLDING_SUCCEEDED = 1;

    /** Constant <code>THRESHOLDING_FAILED=2</code> */
    public static final int THRESHOLDING_FAILED = 2;

    /** Constant <code>statusType="{ Unknown, THRESHOLDING_SUCCEEDED, THRE"{trunked}</code> */
    public static final String[] statusType = { "Unknown", "THRESHOLDING_SUCCEEDED", "THRESHOLDING_FAILED" };

    /**
     * <p>initialize</p>
     *
     * @param parameters a {@link java.util.Map} object.
     */
    public void initialize(Map<?,?> parameters);
    
    /**
     * Called when configurations have changed and need to be refreshed at the ServiceThresolder level.
     * Should not do a "full" initialization, but just reload any config objects that might have
     * incorrect cached data.  It is up to the caller to call "release/initialize" for any interfaces
     * that need reinitialization, and it is recommended to do so *after* calling reinitialize(), so that
     * any objects that might be used in initializing the interfaces have been reloaded.
     */
    public void reinitialize();

    /**
     * <p>release</p>
     */
    public void release();

    /**
     * <p>initialize</p>
     *
     * @param iface a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     * @param parameters a {@link java.util.Map} object.
     */
    public void initialize(ThresholdNetworkInterface iface, Map<?,?> parameters);

    /**
     * <p>release</p>
     *
     * @param iface a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     */
    public void release(ThresholdNetworkInterface iface);

    /**
     * <P>
     * Invokes threshold checking on the object.
     * </P>
     *
     * @param iface a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     * @param eproxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     * @param parameters a {@link java.util.Map} object.
     * @return a int.
     */
    public int check(ThresholdNetworkInterface iface, EventProxy eproxy, Map<?,?> parameters);
}
