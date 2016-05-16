package org.opennms.features.minion.dominion;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.minion.OnmsMinion;

public class MinionListener implements Processor {

    private MinionDao minionDao;

    public void process(Exchange exchange) throws Exception {

        final String minionHandle = exchange.getIn().getBody(String.class);
        //String minionId = minionHandle.getId();
        //String minionLocation = minionHandle.getLocation();
        OnmsMinion minion;
        if (minionDao != null) {
            minion = minionDao.findById(minionHandle);
            if (minion == null) {
                minion = new OnmsMinion();
                minion.setId(minionHandle);
                minion.setLocation("localhost");
            }
            Date lastCheckedIn = new Date();
            minion.setLastCheckedIn(lastCheckedIn);
            minionDao.saveOrUpdate(minion);
        }
    }

    public MinionDao getMinionDao() {
        return minionDao;
    }

    public void setMinionDao(MinionDao minionDao) {
        this.minionDao = minionDao;
    }

}