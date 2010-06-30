//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * <p>PropertySettingTestSuite class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PropertySettingTestSuite extends TestSuite {
    
    String m_propertyName;
    String m_propertyValue;
    
    /**
     * <p>Constructor for PropertySettingTestSuite.</p>
     *
     * @param propertyName a {@link java.lang.String} object.
     * @param propertyValue a {@link java.lang.String} object.
     */
    public PropertySettingTestSuite(String propertyName, String propertyValue) {
        super();
        m_propertyName = propertyName;
        m_propertyValue = propertyValue;
    }
    
    /**
     * <p>Constructor for PropertySettingTestSuite.</p>
     *
     * @param theClass a {@link java.lang.Class} object.
     * @param name a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param propertyValue a {@link java.lang.String} object.
     */
    public PropertySettingTestSuite(Class<? extends TestCase> theClass, String name, String propertyName, String propertyValue) {
        super(theClass, name);
        m_propertyName = propertyName;
        m_propertyValue = propertyValue;
    }

    /**
     * <p>Constructor for PropertySettingTestSuite.</p>
     *
     * @param theClass a {@link java.lang.Class} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param propertyValue a {@link java.lang.String} object.
     */
    public PropertySettingTestSuite(Class<? extends TestCase> theClass, String propertyName, String propertyValue) {
        super(theClass);
        m_propertyName = propertyName;
        m_propertyValue = propertyValue;
    }

    /**
     * <p>Constructor for PropertySettingTestSuite.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param propertyValue a {@link java.lang.String} object.
     */
    public PropertySettingTestSuite(String name, String propertyName, String propertyValue) {
        super(name);
        m_propertyName = propertyName;
        m_propertyValue = propertyValue;
    }
    
    /** {@inheritDoc} */
    public void runTest(Test test, TestResult result) {
        setProperty();
        super.runTest(test, result);
    }

    private void setProperty() {
        System.setProperty(m_propertyName, m_propertyValue);
    }
    
    

}
