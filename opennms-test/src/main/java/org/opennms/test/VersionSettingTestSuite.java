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
// Modifications:
//
// 2007 Jun 23: Eliminate warning caused by unused variable. - dj@opennms.org
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * <p>VersionSettingTestSuite class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class VersionSettingTestSuite extends TestSuite {
    
    int m_version;

    /**
     * <p>Constructor for VersionSettingTestSuite.</p>
     *
     * @param version a int.
     */
    public VersionSettingTestSuite(int version) {
        super();
        m_version = version;
    }
    
    /**
     * <p>Constructor for VersionSettingTestSuite.</p>
     *
     * @param theClass a {@link java.lang.Class} object.
     * @param name a {@link java.lang.String} object.
     * @param version a int.
     */
    public VersionSettingTestSuite(Class theClass, String name, int version) {
        super(theClass, name);
        m_version = version;
        checkForVersionMethod(theClass);
    }

    /**
     * <p>Constructor for VersionSettingTestSuite.</p>
     *
     * @param theClass a {@link java.lang.Class} object.
     * @param version a int.
     */
    public VersionSettingTestSuite(Class theClass, int version) {
        super(theClass);
        m_version = version;
        checkForVersionMethod(theClass);
    }

    /**
     * <p>Constructor for VersionSettingTestSuite.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param version a int.
     */
    public VersionSettingTestSuite(String name, int version) {
        super(name);
        m_version = version;
    }
    
    private void checkForVersionMethod(final Class theClass) {
        try {
            theClass.getMethod("setVersion", new Class[] { Integer.TYPE });
        } catch (NoSuchMethodException e) {
            addTest(new TestCase("warning") {
                protected void runTest() {
                    fail("Unable to locate setVersion method in class "+theClass.getName());
                }
            });
        }
            
    }

    /** {@inheritDoc} */
    public void runTest(Test test, TestResult result) {
        setVersion(test);
        super.runTest(test, result);
    }

    private void setVersion(Test test) {
        try {
            Method m = test.getClass().getMethod("setVersion", new Class[] { Integer.TYPE });
            m.invoke(test, new Object[] { new Integer(m_version) });
        } catch (NoSuchMethodException e) {
            
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    

}
