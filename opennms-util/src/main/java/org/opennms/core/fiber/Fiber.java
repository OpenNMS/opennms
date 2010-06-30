//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//
//

package org.opennms.core.fiber;

/**
 * <p>
 * The <code>Fiber</code> interface is similar to the core Java language
 * {@link java.lang.Thread Thread}class. The <code>Fiber</code> class is used
 * to define a working <em>context</em>, which is the basic feature of a
 * <code>Thread</code>. The differences end there since the
 * <code>Thread</code> class is part of the core language and can only be
 * extended since it is a concrete class.
 * </p>
 *
 * <p>
 * The <code>Fiber</code> concept is used to represent an implementation
 * defined execution context outside of the core Java language. It provides a
 * very loose definition of what and how a <code>Fiber</code> should behave.
 * This gives a great deal of implementation independence to the implementing
 * classes.
 * </p>
 *
 * <p>
 * For example, the <code>Fiber</code> interface could be used to represent a
 * one-to-one mapping of a Java <code>Thread</code>. It could be used to
 * represent a thread pool where multiple threads are grouped as one
 * <code>Fiber</code> unit. Additionally, it could be used to defined a new
 * execution environment where multiple <em>Runnable</em> elements are
 * multiplexed over a one or more threads, where the <em>Runnables</em> far
 * exceed the core Java threads.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public interface Fiber {
    /**
     * The string names that correspond to the states of the fiber.
     */
    public static final String STATUS_NAMES[] = {
        "START_PENDING", // 0
        "STARTING", // 1
        "RUNNING", // 2
        "STOP_PENDING", // 3
        "STOPPED", // 4
        "PAUSE_PENDING", // 5
        "PAUSED", // 6
        "RESUME_PENDING" // 7
    };

    /**
     * This is the initial <code>Fiber</code> state. When the
     * <code>Fiber</code> begins it startup process it will transition to the
     * <code>STARTING</code> state. A <code>Fiber</code> in a start pending
     * state has not begun any of the initilization process.
     */
    public static final int START_PENDING = 0;

    /**
     * This state is used to define when a <code>Fiber</code> has begun the
     * initilization process. Once the initilization process is completed the
     * <code>Fiber</code> will transition to a <code>RUNNING</code> status.
     */
    public static final int STARTING = 1;

    /**
     * This state is used to define the normal runtime condition of a
     * <code>Fiber</code>. When a <code>Fiber</code> is in this state then
     * it is processing normally.
     */
    public static final int RUNNING = 2;

    /**
     * This state is used to denote when the <code>Fiber</code> is terminating
     * processing. This state is always followed by the state
     * <code>ST0PPED</code>.
     */
    public static final int STOP_PENDING = 3;

    /**
     * This state represents the final resting state of a <code>Fiber</code>.
     * Depending on the implementation it may be possible to resurect the
     * <code>Fiber</code> from this state.
     */
    public static final int STOPPED = 4;

    /**
     * This method is used to start the initilization process of the
     * <code>Fiber</code>, which should eventually transition to a
     * <code>RUNNING</code> status.
     */
    public void start();

    /**
     * This method is used to stop a currently running <code>Fiber</code>.
     * Once invoked the <code>Fiber</code> should begin it's shutdown process.
     * Depending on the implementation, this method may block until the
     * <code>Fiber</code> terminates.
     */
    public void stop();

    /**
     * This method is used to return the name of the <code>Fiber</code>. The
     * name of the instance is defined by the implementor, but it should be
     * realitively unique when possible.
     *
     * @return The name of the <code>Fiber</code>.
     */
    public String getName();

    /**
     * This method is used to get the current status of the <code>Fiber</code>.
     * The status of the fiber should be one of the predefined constants of the
     * <code>Fiber</code> interface, or from one of the derived interfaces.
     *
     * @return The current status of the <code>Fiber</code>.
     */
    public int getStatus();
}
