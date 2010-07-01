//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi.wbem.jinterop;

import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.common.JIException;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;

/**
 * <p>OnmsWbemPropertySetImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemPropertySetImpl implements OnmsWbemPropertySet {
    private IJIDispatch wbemPropertySetDispatch;

    /**
     * <p>Constructor for OnmsWbemPropertySetImpl.</p>
     *
     * @param wbemPropertySetDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemPropertySetImpl(IJIDispatch wbemPropertySetDispatch) {
        this.wbemPropertySetDispatch = wbemPropertySetDispatch;
    }

    /**
     * <p>count</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Integer count() throws WmiException {
        try {
            JIVariant Count = wbemPropertySetDispatch.get("Count");
            return Count.getObjectAsInt();
        } catch (JIException e) {
            throw new WmiException("Retrieving Count failed: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    public OnmsWbemProperty get(Integer idx) throws WmiException {
        try {
            IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            IJIEnumVariant enumVariant =
                    (IJIEnumVariant) JIObjectFactory.narrowObject(
                            enumComObject.queryInterface(IJIEnumVariant.IID));
            OnmsWbemProperty wbemObj;
            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < (idx+1); i++) {
                Object[] values = enumVariant.next(1);
                JIArray array = (JIArray)values[0];
                Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++)
                {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());
                }
            }

            wbemObj = new OnmsWbemPropertyImpl(wbemProperty_dispatch);
            return wbemObj;
        } catch(JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    public OnmsWbemProperty getByName(String name) throws WmiException {
        try {
            IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            IJIEnumVariant enumVariant =
                    (IJIEnumVariant) JIObjectFactory.narrowObject(
                            enumComObject.queryInterface(IJIEnumVariant.IID));

            OnmsWbemProperty wbemObj = null;
            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < count(); i++) {
                Object[] values = enumVariant.next(1);
                JIArray array = (JIArray)values[0];
                Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++)
                {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());

                    // Check the name
                    JIVariant variant = wbemProperty_dispatch.get("Name");
                    if(variant.getObjectAsString2().equalsIgnoreCase(name))
                        return new OnmsWbemPropertyImpl(wbemProperty_dispatch);
                }
            }

            throw new WmiException("Property Name '" + name + "' not found.");
        } catch(JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }
}
