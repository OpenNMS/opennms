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
 * This interface defines an extension to the core {@link Fiber Fiber}interface
 * and adds the methods for pausing and resuming an executable context. The base
 * interface class defines an execution environment, this interface extends the
 * core interface to provide methods for suspending and resuming the
 * <code>Fiber</code>.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface PausableFiber extends Fiber {
    /**
     * This state is used to define when a <code>Fiber</code> has begun the
     * process of pausing its operations. This is the intermedate period where
     * the thread is no longer in the <code>RUNNING</code> status, but not yet
     * to a <code>PAUSED</code> status.
     */
    public static final int PAUSE_PENDING = 5;

    /**
     * This state is used to denote a paused, or otherwise suspended
     * <code>Fiber</code>. When a <code>Fiber</code> is in this state it
     * should not be preforming any work.
     */
    public static final int PAUSED = 6;

    /**
     * This state is used to denote a <code>Fiber</code> recovering from a
     * paused state to a running status. During this status the
     * <code>Fiber</code> is reinitializing any necessary internal elements to
     * re-enter the <code>RUNNING</code> state.
     */
    public static final int RESUME_PENDING = 7;

    /**
     * This method is used to suspend a currently running <code>Fiber</code>.
     * When invoked the <code>Fiber</code> will begin the transition to
     * a <code>PAUSED</code> status after changing its internal state, if
     * applicable.
     */
    void pause();

    /**
     * This method is used to resume a suspeneded <code>Fiber</code>. If the
     * thread is already running then this method should have no effect on the
     * current <code>Fiber</code>.
     */
    void resume();
}
