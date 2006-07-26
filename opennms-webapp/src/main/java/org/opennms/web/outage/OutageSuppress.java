package org.opennms.web.outage;

import java.sql.Date;

public class OutageSuppress {
    
    public void SuppressOutage (Integer outageID, Date Time, String suppressedBy) {
        // Some quirks, if time is == 0 - We will set this to 
        // the largest possible date that we can come up with
        //    
    }
    
    public void UnSuppressOutage (Integer outageID, String suppressedBy) {
        // Need no time really....
        // We'll actually just delete the suppresstimefield
    
    }
    
    public void SubmitOutageSuppressedEvent(Integer outageID, Date suppressTime, String suppressedBy){

    }
    
    public void SubmitUnSuppressedEvent (Integer outageID, String suppressedBy){
        
    }

}
