package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface SurveillanceService extends RemoteService {

    public SurveillanceData getSurveillanceData();
}
