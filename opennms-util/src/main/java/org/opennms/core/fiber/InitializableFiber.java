/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.fiber;

/**
 * <p>
 * This class is used to extend the <code>Fiber</code> interface so that is
 * has a concept of a life cycle. Prior to starting the fiber the
 * <code>init</code> method will be invoked. Likewise, prior to garbage
 * collection the <code>destroy</code> method should be invoked.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface InitializableFiber extends Fiber {
    /**
     * This method is used to start the initilization process of the
     * <code>Fiber</code>, which should eventually transition to a
     * <code>RUNNING</code> status.
     */
    public void init();

    /**
     * This method is used to stop a currently running <code>Fiber</code>.
     * Once invoked the <code>Fiber</code> should begin it's shutdown process.
     * Depending on the implementation, this method may block until the
     * <code>Fiber</code> terminates.
     */
    public void destroy();
}
