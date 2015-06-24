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

package org.opennms.netmgt.mock;

/**
 * <p>MockVisitor interface.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface MockVisitor {

    /**
     * <p>visitContainer</p>
     *
     * @param c a {@link org.opennms.netmgt.mock.MockContainer} object.
     */
    public void visitContainer(MockContainer<?,?> c);

    /**
     * <p>visitElement</p>
     *
     * @param e a {@link org.opennms.netmgt.mock.MockElement} object.
     */
    public void visitElement(MockElement e);

    /**
     * <p>visitInterface</p>
     *
     * @param i a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public void visitInterface(MockInterface i);

    /**
     * <p>visitNetwork</p>
     *
     * @param n a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
    public void visitNetwork(MockNetwork n);

    /**
     * <p>visitNode</p>
     *
     * @param n a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public void visitNode(MockNode n);

    /**
     * <p>visitService</p>
     *
     * @param s a {@link org.opennms.netmgt.mock.MockService} object.
     */
    public void visitService(MockService s);

    /**
     * <p>visitPathOutage</p>
     * 
     * @param m_currentOutage a {@link org.opennms.netmgt.mock.MockPathOutage} object.
     */
	public void visitPathOutage(MockPathOutage m_currentOutage);

}
