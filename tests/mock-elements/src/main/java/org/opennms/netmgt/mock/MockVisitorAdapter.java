/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

/**
 * <p>MockVisitorAdapter class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public class MockVisitorAdapter implements MockVisitor {

    /** {@inheritDoc} */
    @Override
    public void visitContainer(MockContainer<?,?> n) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitElement(MockElement e) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitInterface(MockInterface i) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNetwork(MockNetwork n) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitNode(MockNode n) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitService(MockService s) {
    }

}
