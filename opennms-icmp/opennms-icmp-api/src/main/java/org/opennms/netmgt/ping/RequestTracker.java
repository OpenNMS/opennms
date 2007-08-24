package org.opennms.netmgt.ping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;

public class RequestTracker {

    /**
     * The map of long thread identifiers to Packets that must be signaled.
     * The mapped objects are instances of the
     * {@link org.opennms.netmgt.ping.Reply Reply} class.
     */
    private static Map<Long, PingRequest> waiting = Collections.synchronizedMap(new TreeMap<Long, PingRequest>());
    private FifoQueueImpl<Reply> m_queue;
    public static final int DEFAULT_WAIT_TIME = 50;

    public RequestTracker(FifoQueueImpl<Reply> queue) {
        m_queue = queue;
    }

    public Map<Long, PingRequest> getPendingRequestMap() {
        return waiting;
    }

    public Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    long getNextExpirationTime() {
        ArrayList<PingRequest> pr = null;
        synchronized(getTrackerLock()) {
    	    if (getPendingRequestMap().size() == 0) {
    	        return DEFAULT_WAIT_TIME;
    	    }
    	    
            pr = new ArrayList<PingRequest>(getPendingRequestMap().values());
    	}
        Collections.sort(pr, new Comparator<PingRequest>() {
            public int compare(PingRequest arg0, PingRequest arg1) {
                if (arg0 == null) {
                    return -1;
                } else if (arg1 == null) {
                    return 1;
                } else if (arg0.getExpiration() == arg0.getExpiration()) {
                    return 0;
                } else {
                    return (arg1.getExpiration() > arg0.getExpiration()? 1 : -1);
                }
            }
        });
        log().info(System.currentTimeMillis() + ": " + pr.size() + " pending requests, lowest is " + pr.get(0));
        long waitTime = pr.get(0).getExpiration() - System.currentTimeMillis();
        if (waitTime < 0) {
            return 0;
        } else {
            return waitTime;
        }
    }
    
    public Object getTrackerLock() {
        return this;
    }

    public Reply getNextReply() throws InterruptedException {
        long waitTime = getNextExpirationTime();
        log().info("minimum wait time: " + waitTime);
        if (waitTime > 0) {
            return m_queue.remove(waitTime);
        }
        return null;
    }

    synchronized void registerRequest(PingRequest request) {
        getPendingRequestMap().put(request.getTid(), request);
    }
    
    
}
