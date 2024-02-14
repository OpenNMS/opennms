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
/**
 * 
 */
package org.opennms.netmgt.asterisk.agi.scripts;

import java.net.InetAddress;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.opennms.core.utils.InetAddressUtils;

/**
 * <p>Abstract BaseOnmsAgiScript class.</p>
 *
 * @author jeffg
 * @version $Id: $
 */
public abstract class BaseOnmsAgiScript extends BaseAgiScript {


    /** Constant <code>VAR_INTERRUPT_DIGITS="INTERRUPT_DIGITS"</code> */
    protected static final String VAR_INTERRUPT_DIGITS = "INTERRUPT_DIGITS";
    /** Constant <code>VAR_OPENNMS_INTERFACE="OPENNMS_INTERFACE"</code> */
    public static final String VAR_OPENNMS_INTERFACE = "OPENNMS_INTERFACE";
    /** Constant <code>VAR_OPENNMS_SERVICE="OPENNMS_SERVICE"</code> */
    public static final String VAR_OPENNMS_SERVICE = "OPENNMS_SERVICE";
    /** Constant <code>VAR_OPENNMS_NODEID="OPENNMS_NODEID"</code> */
    public static final String VAR_OPENNMS_NODEID = "OPENNMS_NODEID";
    /** Constant <code>VAR_OPENNMS_NODELABEL="OPENNMS_NODELABEL"</code> */
    public static final String VAR_OPENNMS_NODELABEL = "OPENNMS_NODELABEL";
    /** Constant <code>VAR_OPENNMS_NOTIFY_SUBJECT="OPENNMS_NOTIFY_SUBJECT"</code> */
    public static final String VAR_OPENNMS_NOTIFY_SUBJECT = "OPENNMS_NOTIFY_SUBJECT";
    /** Constant <code>VAR_OPENNMS_NOTIFY_BODY="OPENNMS_NOTIFY_BODY"</code> */
    public static final String VAR_OPENNMS_NOTIFY_BODY = "OPENNMS_NOTIFY_BODY";
    /** Constant <code>VAR_OPENNMS_USER_PIN="OPENNMS_USER_PIN"</code> */
    public static final String VAR_OPENNMS_USER_PIN = "OPENNMS_USER_PIN";
    /** Constant <code>VAR_OPENNMS_USERNAME="OPENNMS_USERNAME"</code> */
    public static final String VAR_OPENNMS_USERNAME = "OPENNMS_USERNAME";
    
    /**
     * <p>sayAlphaInterruptible</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayAlphaInterruptible(String text) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayAlpha(text, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayAlpha(text);
            return 0x0;
        }
    }
    
    /**
     * <p>sayDateTimeInterruptible</p>
     *
     * @param time a long.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayDateTimeInterruptible(long time) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayDateTime(time, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayDateTime(time);
            return 0x0;
        }
    }
    
    /**
     * <p>sayDigitsInterruptible</p>
     *
     * @param digits a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayDigitsInterruptible(String digits) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayDigits(digits, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayDigits(digits);
            return 0x0;
        }
    }
    
    /**
     * <p>sayNumberInterruptible</p>
     *
     * @param number a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayNumberInterruptible(String number) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayNumber(number, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayNumber(number);
            return 0x0;
        }
    }
    
    /**
     * <p>sayPhoneticInterruptible</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayPhoneticInterruptible(String text) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayPhonetic(text, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayPhonetic(text);
            return 0x0;
        }
    }
    
    /**
     * <p>sayTimeInterruptible</p>
     *
     * @param time a long.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayTimeInterruptible(long time) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayTime(time, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayTime(time);
            return 0x0;
        }
    }
    
    /**
     * <p>sayIpAddressInterruptible</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayIpAddressInterruptible(InetAddress addr) throws AgiException {
        char pressed;
        // FIXME: this needs to be IPv6-compatible
        for (String octet : addr.getHostAddress().split("\\.")) {
            pressed = sayDigitsInterruptible(octet);
            if (pressed != 0x0)
                return pressed;
            pressed = sayAlphaInterruptible(".");
            if (pressed != 0x0)
                return pressed;
        }
        return 0x0;
    }
    
    /**
     * <p>sayIpAddressInterruptible</p>
     *
     * @param addrString a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char sayIpAddressInterruptible(String addrString) throws AgiException {
    	
    	final InetAddress addr = InetAddressUtils.addr(addrString);
    	if (addr == null) {
    		return 0x0;
    	}
		return sayIpAddressInterruptible(addr);
    }
    
    /**
     * <p>streamFileInterruptible</p>
     *
     * @param file a {@link java.lang.String} object.
     * @return a char.
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    protected char streamFileInterruptible(String file) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return streamFile(file, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            streamFile(file);
            return 0x0;
        }
    }
}
