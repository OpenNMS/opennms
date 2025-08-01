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
