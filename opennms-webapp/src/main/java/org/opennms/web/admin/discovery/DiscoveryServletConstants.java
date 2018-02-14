/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.discovery;

/**
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DiscoveryServletConstants {

    /**
     * Indicates that we're editing the persistent discovery config
     */
    public static final String EDIT_MODE_CONFIG = "config";

    /**
     * Indicates that we're editing a one-time scan config
     */
    public static final String EDIT_MODE_SCAN = "scan";

    /** Constant <code>addSpecificAction="AddSpecific"</code> */
    public static final String addSpecificAction = "AddSpecific";
    /** Constant <code>removeSpecificAction="RemoveSpecific"</code> */
    public static final String removeSpecificAction = "RemoveSpecific";

    /** Constant <code>addIncludeRangeAction="AddIncludeRange"</code> */
    public static final String addIncludeRangeAction = "AddIncludeRange";
    /** Constant <code>removeIncludeRangeAction="RemoveIncludeRange"</code> */
    public static final String removeIncludeRangeAction = "RemoveIncludeRange";

    /** Constant <code>addIncludeUrlAction="AddIncludeUrl"</code> */
    public static final String addIncludeUrlAction = "AddIncludeUrl";
    /** Constant <code>removeIncludeUrlAction="RemoveIncludeUrl"</code> */
    public static final String removeIncludeUrlAction = "RemoveIncludeUrl";

    /** Constant <code>addExcludeRangeAction="AddExcludeRange"</code> */
    public static final String addExcludeRangeAction = "AddExcludeRange";
    /** Constant <code>removeExcludeRangeAction="RemoveExcludeRange"</code> */
    public static final String removeExcludeRangeAction = "RemoveExcludeRange";

    /** Constant <code>saveAndRestartAction="SaveAndRestart"</code> */
    public static final String saveAndRestartAction = "SaveAndRestart";
}
