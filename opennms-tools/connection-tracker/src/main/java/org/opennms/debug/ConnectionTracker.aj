package org.opennms.debug;

import javax.sql.PooledConnection;

public aspect ConnectionTracker {
    pointcut checkoutConnection() : call(* com.mchange.v2.resourcepool.ResourcePool.checkoutResource(..));
    pointcut checkinConnection(Object c) : call(* com.mchange.v2.resourcepool.ResourcePool.checkinResource(..)) && args(c);

    after() returning (Object c) : checkoutConnection() {
        if (c instanceof PooledConnection) Connections.track((PooledConnection)c);
    }

    before(Object c) : checkinConnection(c) {
        if (c instanceof PooledConnection) Connections.complete((PooledConnection)c);
    }

}
