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
import org.opennms.protocols.wmi.wbem.OnmsWbemMethod;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;

/**
 * <p>OnmsWbemMethodSetImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemMethodSetImpl implements OnmsWbemMethodSet {
    private IJIDispatch wbemMethodSetDispatch;

    /**
     * <p>Constructor for OnmsWbemMethodSetImpl.</p>
     *
     * @param wbemMethodSetDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemMethodSetImpl(final IJIDispatch wbemMethodSetDispatch) {
        this.wbemMethodSetDispatch = wbemMethodSetDispatch;
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Integer getCount() throws WmiException {
        try {
            return wbemMethodSetDispatch.get("Count").getObjectAsInt();
        } catch(final JIException e) {
            throw new WmiException("Failed to retrieve Method Set count: " + e.getMessage(), e);
        }
    }

        /** {@inheritDoc} */
    @Override
        public OnmsWbemMethod get(final Integer idx) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemMethodSetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
            OnmsWbemMethod wbemMethod;
            IJIDispatch wbemMethod_dispatch = null;
            for (int i = 0; i < (idx+1); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++)
                {
                    wbemMethod_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());
                }
            }

            wbemMethod = new OnmsWbemMethodImpl(wbemMethod_dispatch);
            return wbemMethod;
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemObject variant: " + e.getMessage(), e);
        }
    }

}
