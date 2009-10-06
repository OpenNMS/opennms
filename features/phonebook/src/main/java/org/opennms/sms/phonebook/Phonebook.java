package org.opennms.sms.phonebook;

public interface Phonebook {

    /**
     * Get an SMS message target given an IP address.
     * 
     * @param address the IP address
     * @return a string representing the SMS "to" (usually a phone number or SMS email address)
     * @throws @{link PhonebookException}
     */
    String getTargetForAddress(String address) throws PhonebookException;

}
