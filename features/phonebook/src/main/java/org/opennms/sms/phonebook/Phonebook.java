package org.opennms.sms.phonebook;

/**
 * <p>Phonebook interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface Phonebook {

    /**
     * Get an SMS message target given an IP address.
     *
     * @param address the IP address
     * @return a string representing the SMS "to" (usually a phone number or SMS email address)
     * @throws @{link PhonebookException}
     * @throws org.opennms.sms.phonebook.PhonebookException if any.
     */
    String getTargetForAddress(String address) throws PhonebookException;

}
