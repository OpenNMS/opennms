/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    /** {@inheritDoc} */
	@Override
	public void visitPathOutage(MockPathOutage m_currentOutage) {		
	}

}
