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

package org.opennms.netmgt.poller.pollables;

/**
 * Represents a PollableVisitor
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollableVisitor {

    /**
     * <p>visitService</p>
     *
     * @param service a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    void visitService(PollableService service);

    /**
     * <p>visitInterface</p>
     *
     * @param interface1 a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    void visitInterface(PollableInterface interface1);

    /**
     * <p>visitNode</p>
     *
     * @param node a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    void visitNode(PollableNode node);

    /**
     * <p>visitNetwork</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    void visitNetwork(PollableNetwork network);

    /**
     * <p>visitContainer</p>
     *
     * @param container a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     */
    void visitContainer(PollableContainer container);

    /**
     * <p>visitElement</p>
     *
     * @param element a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    void visitElement(PollableElement element);

}
