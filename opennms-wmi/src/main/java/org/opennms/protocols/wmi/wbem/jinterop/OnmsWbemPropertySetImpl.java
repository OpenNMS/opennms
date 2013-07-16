/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
    public OnmsWbemPropertySetImpl(final IJIDispatch wbemPropertySetDispatch) {
        this.wbemPropertySetDispatch = wbemPropertySetDispatch;
    }

    /**
     * <p>count</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Integer count() throws WmiException {
        try {
            final JIVariant Count = wbemPropertySetDispatch.get("Count");
            return Count.getObjectAsInt();
        } catch (final JIException e) {
            throw new WmiException("Retrieving Count failed: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemProperty get(final Integer idx) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
            OnmsWbemProperty wbemObj;
            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < (idx+1); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++) {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());
                }
            }

            wbemObj = new OnmsWbemPropertyImpl(wbemProperty_dispatch);
            return wbemObj;
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemProperty getByName(final String name) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));

            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < count(); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++) {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());

                    // Check the name
                    final JIVariant variant = wbemProperty_dispatch.get("Name");
                    if(variant.getObjectAsString2().equalsIgnoreCase(name)) {
                        return new OnmsWbemPropertyImpl(wbemProperty_dispatch);
                    }
                }
            }

            throw new WmiException("Property Name '" + name + "' not found.");
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }
}
