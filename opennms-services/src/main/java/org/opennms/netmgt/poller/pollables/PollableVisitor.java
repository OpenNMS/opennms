/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created December 31, 2004
 *
 * Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
