/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
    @Override
    public void runTest(Test test, TestResult result) {
        setProperty();
        super.runTest(test, result);
    }

    private void setProperty() {
        System.setProperty(m_propertyName, m_propertyValue);
    }
    
    

}
