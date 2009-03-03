package org.opennms.netmgt.config;


public interface RancidAdapterConfig {
    
    /**
     * return the delay time for the specified address
     * the delay time is the time in msec that represents
     * a delay in the execution of a RancidAdapter
     * execution
     * @param ipaddress
     *          the ipaddress of the node
     * @return the delay time
     */
    public abstract long getDelay(String ipaddress);

    /**
     * return the number of retries in case of failure
     * for the specified address
     * @param ipaddress
     *          the ipaddress of the node
     * @return the number of retries 
     */
    public abstract int getRetries(String ipaddress);
    
    /**
     * return the delay time for the specified address
     * the retrydelay time is the time in msec that represents
     * a delay in the execution of a RancidAdapter
     * execution retry after a failure
     * @param ipaddress
     *          the ipaddress of the node
     * @return the delay time for retry
     */
    public abstract long getRetryDelay(String ipaddress);
       
    /**
     * return if is to be used the opennms categories to get 
     * rancid device type
     * @param ipaddress 
     *          the ipaddress of the node
     * @return true if use opennms category
     */
    public abstract boolean useCategories(String ipaddress);

    /**
     * return the Rancid Type String
     * @param sysoid 
     *          the system OID identifier of the node
     * @return RancidType String
     */
    public abstract String getType(String sysoid);  

    /**
     * Return if current time is part of specified outage.
     * 
     * @param ipaddress
     *          the ipaddress of the node
     * 
     * @return true if current time is in a schedules under policy manage
     */
    public abstract boolean isCurTimeInSchedule(String ipaddress);     

}
