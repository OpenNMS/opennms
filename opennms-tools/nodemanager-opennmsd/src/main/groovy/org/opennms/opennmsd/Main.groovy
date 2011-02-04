package org.opennms.opennmsd;

import org.apache.log4j.Logger;

public class Main {
    
    private static Logger log = Logger.getLogger(Main.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            log.info("Starting opennmsd");
            OpenNMSDaemon daemon = new OpenNMSDaemon();
            daemon.setConfiguration(new DefaultConfiguration());
            daemon.setEventForwarder(new DefaultEventForwarder());
            daemon.execute();
        } catch(Throwable e) {
            log.error("Exception executing opennmsd", e);
            System.exit(27);
        }
        
        System.exit(0);
        
    }

}