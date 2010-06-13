/**
 * 
 */
package org.opennms.netmgt.asterisk.agi.scripts;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author jeffg
 *
 */
public abstract class BaseOnmsAgiScript extends BaseAgiScript {

    protected static final String VAR_INTERRUPT_DIGITS = "INTERRUPT_DIGITS";
    public static final String VAR_OPENNMS_INTERFACE = "OPENNMS_INTERFACE";
    public static final String VAR_OPENNMS_SERVICE = "OPENNMS_SERVICE";
    public static final String VAR_OPENNMS_NODEID = "OPENNMS_NODEID";
    public static final String VAR_OPENNMS_NODELABEL = "OPENNMS_NODELABEL";
    public static final String VAR_OPENNMS_NOTIFY_SUBJECT = "OPENNMS_NOTIFY_SUBJECT";
    public static final String VAR_OPENNMS_NOTIFY_BODY = "OPENNMS_NOTIFY_BODY";
    public static final String VAR_OPENNMS_USER_PIN = "OPENNMS_USER_PIN";
    public static final String VAR_OPENNMS_USERNAME = "OPENNMS_USERNAME";
    
    protected char sayAlphaInterruptible(String text) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayAlpha(text, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayAlpha(text);
            return 0x0;
        }
    }
    
    protected char sayDateTimeInterruptible(long time) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayDateTime(time, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayDateTime(time);
            return 0x0;
        }
    }
    
    protected char sayDigitsInterruptible(String digits) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayDigits(digits, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayDigits(digits);
            return 0x0;
        }
    }
    
    protected char sayNumberInterruptible(String number) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayNumber(number, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayNumber(number);
            return 0x0;
        }
    }
    
    protected char sayPhoneticInterruptible(String text) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayPhonetic(text, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayPhonetic(text);
            return 0x0;
        }
    }
    
    protected char sayTimeInterruptible(long time) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return sayTime(time, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            sayTime(time);
            return 0x0;
        }
    }
    
    protected char sayIpAddressInterruptible(InetAddress addr) throws AgiException {
        char pressed;
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
    
    protected char sayIpAddressInterruptible(String addrString) throws AgiException {
        try {
            return sayIpAddressInterruptible(InetAddress.getByName(addrString));
        } catch (UnknownHostException uhe) {
            try {
                return sayIpAddressInterruptible(InetAddress.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                return 0x0;
            }
        }
    }
    
    protected char streamFileInterruptible(String file) throws AgiException {
        if (! "".equals(getVariable(VAR_INTERRUPT_DIGITS))) {
            return streamFile(file, getVariable(VAR_INTERRUPT_DIGITS));
        } else {
            streamFile(file);
            return 0x0;
        }
    }
    
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
