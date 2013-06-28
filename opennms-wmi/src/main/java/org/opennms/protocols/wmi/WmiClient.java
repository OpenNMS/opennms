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

package org.opennms.protocols.wmi;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

import org.opennms.protocols.wmi.wbem.OnmsWbemFlagReturnEnum;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.jinterop.OnmsWbemObjectImpl;
import org.opennms.protocols.wmi.wbem.jinterop.OnmsWbemObjectSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This is a low-level WMI client harnessing DCOM to communicate with remote agents.
 * The interface provided is similar but not identical to that of the SWbemServices
 * interface.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class WmiClient implements IWmiClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(WmiClient.class);


    private JIComServer m_ComStub = null;
    private IJIComObject m_ComObject = null;
    private IJIDispatch m_Dispatch = null;
    private String m_Address = null;
    private JISession m_Session = null;
    private IJIDispatch m_WbemServices = null;

    private static final String WMI_CLSID = "76A6415B-CB41-11d1-8B02-00600806D9B6";
    private static final String WMI_PROGID = "WbemScripting.SWbemLocator";

    /**
     * <p>Constructor for WmiClient.</p>
     *
     * @param address a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public WmiClient(final String address) throws WmiException {
        JISystem.setAutoRegisteration(true);
        JISystem.getLogger().setLevel(Level.OFF);
        m_Address = address;
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemObjectSet performInstanceOf(final String wmiClass) throws WmiException {
        try {
            // Execute the InstancesOf method on the remote SWbemServices object.
            final JIVariant results[] = m_WbemServices.callMethodA("InstancesOf", new Object[]{new JIString(wmiClass), 0, JIVariant.OPTIONAL_PARAM()});
            final IJIDispatch wOSd = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(wOSd);

        } catch (final JIException e) {
            throw new WmiException("Failed to perform WMI operation (\\\\" + wmiClass + ") : " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemObjectSet performExecQuery(final String strQuery) throws WmiException {
        return performExecQuery(strQuery, "WQL", OnmsWbemFlagReturnEnum.wbemFlagReturnImmediately.getReturnFlagValue());
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsWbemObjectSet performExecQuery (final String strQuery, final String strQueryLanguage, final Integer flags) throws WmiException {
        try {
            final JIVariant results[] = m_WbemServices.callMethodA("ExecQuery", new Object[]{new JIString(strQuery), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM()});
            final IJIDispatch wOSd = (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(wOSd);
        } catch(final JIException e) {
            throw new WmiException("Failed to execute query '" + strQuery + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>performWmiGet</p>
     *
     * @param strObjectPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObject} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObject performWmiGet(final String strObjectPath) throws WmiException {
        try {
            final JIVariant results[] = m_WbemServices.callMethodA("Get", new Object[]{new JIString(strObjectPath), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            final IJIDispatch obj_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectImpl(obj_dsp);
        } catch (final JIException e) {
            throw new WmiException("Failed to perform get '" + strObjectPath + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>performSubclassOf</p>
     *
     * @param strSuperClass a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performSubclassOf(final String strSuperClass) throws WmiException {
        try {
            final JIVariant results[] = m_WbemServices.callMethodA("SubclassesOf", new Object[]{new JIString(strSuperClass), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            final IJIDispatch objset_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
            
            return new OnmsWbemObjectSetImpl(objset_dsp);
        } catch (final JIException e) {
            throw new WmiException("Failed to perform SubclassesOf '" + strSuperClass + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>performSubclassOf</p>
     *
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performSubclassOf() throws WmiException {
        try {
            final JIVariant results[] = m_WbemServices.callMethodA("SubclassesOf", new Object[]{ JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            final IJIDispatch objset_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(objset_dsp);
        } catch (final JIException e) {
            throw new WmiException("Failed to perform SubclassesOf: " + e.getMessage(), e);
        }
    }

    /**
     * <p>convertToNativeType</p>
     *
     * @param type a {@link org.jinterop.dcom.core.JIVariant} object.
     * @return a {@link java.lang.Object} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public static Object convertToNativeType(final JIVariant type) throws WmiException {
        try {
            if (type.isArray()) {
                final ArrayList<Object> objs = new ArrayList<Object>();
                final Object [] array = (Object[])type.getObjectAsArray().getArrayInstance();

                for (final Object element : array) {
                    objs.add(convertToNativeType((JIVariant)element));
                }
                
                return objs;
            }

            switch (type.getType()) {
                case JIVariant.VT_NULL:
                    return null;
                case JIVariant.VT_BSTR:
                    return type.getObjectAsString().getString();
                case JIVariant.VT_I2: // sint16
                    return type.getObjectAsShort();
                case JIVariant.VT_I4:
                    return type.getObjectAsInt();
                case JIVariant.VT_UI1: // uint8 (convert to Java Number)
                    return type.getObjectAsUnsigned().getValue();
                case JIVariant.VT_BOOL:
                    return type.getObjectAsBoolean();
                case JIVariant.VT_DECIMAL:
                    return type.getObjectAsFloat();
                case JIVariant.VT_DATE:
                    return type.getObjectAsDate();
                default:
                    throw new WmiException("Unknown type presented (" + type.getType() + "), defaulting to Object: " + type.toString());
            }
        } catch (final JIException e) {
            throw new WmiException("Failed to conver WMI type to native object: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final String domain, final String username, final String password) throws WmiException {
        try {

            m_Session = JISession.createSession(domain, username, password);
            m_Session.useSessionSecurity(true);
            m_Session.setGlobalSocketTimeout(5000);

            m_ComStub = new JIComServer(JIProgId.valueOf(WMI_PROGID), m_Address, m_Session);

            final IJIComObject unknown = m_ComStub.createInstance();
            m_ComObject = unknown.queryInterface(WMI_CLSID);

            // This will obtain the dispatch interface
            m_Dispatch = (IJIDispatch) JIObjectFactory.narrowObject(m_ComObject.queryInterface(IJIDispatch.IID));
            final JIVariant results[] = m_Dispatch.callMethodA(
                "ConnectServer",
                new Object[]{
                    new JIString(m_Address),
                    JIVariant.OPTIONAL_PARAM(),
                    JIVariant.OPTIONAL_PARAM(),
                    JIVariant.OPTIONAL_PARAM(),
                    JIVariant.OPTIONAL_PARAM(),
                    JIVariant.OPTIONAL_PARAM(),
                    0,
                    JIVariant.OPTIONAL_PARAM()
                }
            );

            m_WbemServices = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

        } catch (final JIException e) {
            if (m_Session != null) {
                try {
                    JISession.destroySession(m_Session);
                } catch (JIException e1) {
                    LOG.error("Failed to destroy session after incomplete connect with host '{}'.", m_Address, e1);
                }
            }
            throw new WmiException("Failed to establish COM session with host '" + m_Address + "': " + e.getMessage(), e);
        } catch (final UnknownHostException e) {
            if (m_Session != null) {
                try {
                    JISession.destroySession(m_Session);
                } catch (JIException e1) {
                    LOG.error("Failed to destroy session after unknown host '{}'.", m_Address, e1);
                }
            }
            throw new WmiException("Unknown host '" + m_Address + "'. Failed to connect to WMI agent.", e);
        }
    }

    /**
     * <p>disconnect</p>
     *
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public void disconnect() throws WmiException {
        try {
            JISession.destroySession(m_Session);
        } catch (JIException e) {
            throw new WmiException("Failed to destroy J-Interop session: " + e.getMessage(), e);
        }
    }

    /**
     * <p>convertWmiDate</p>
     *
     * @param dateStr a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     * @throws java.text.ParseException if any.
     */
    public static Date convertWmiDate(final String dateStr) throws ParseException {
        return new SimpleDateFormat("yyyyMMddHHmmss.ssssss+000").parse(dateStr);
    }

    // TODO This needs to be completed.
    @SuppressWarnings("unused")
    private static boolean isNumeric(final String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

    // TODO this needs to be completed.
    @SuppressWarnings("unused")
    private static boolean isDate(final String str) {
        // Parse the date.
        try {
            DateFormat.getDateInstance().parse(str);
            return true;
        } catch (final ParseException e) {
            return false;
        }
    }
}
