//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//

package org.opennms.netmgt.poller;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.scheduler.ReadyRunnable;

/**
 * <P>A proxy to a PollableService which acts as a surrogate for rescheduling purposes.
 *    This was created as a temporary fix to allow us to schedule a PollableService
 *    for a sooner execution, despite the fact that it is already scheduled for a later
 *    time.  At the time we created this class, the Scheduler did not provide any means
 *    to cancel or reschedule an element on its queue.  Therefore, this workaround became
 *    necessary.</P>
 *
 * <P>In order to use this class, create an instance of it with the desired PollableService
 *    and the timestamp at which you want it to run.  Once you have the instance, you can
 *    schedule it with the Scheduler, since it is of type ReadyRunnable.</P>
 *
 * <P>Each time it runs, it will reschedule itself at the interval currently valid for the
 *    PollableService it contains.  Once it catches up to the originally scheduled time
 *    for its PollableService, it will politely quit rescheduling itself.  Assuming that
 *    the scheduler was the only object that was keeping a reference to this proxy, it
 *    should be noticed by the garbage collector next time runs.</P>
 *
 * @author <A HREF="mailto:justis@opennms.org">Justis Peters</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see PollableService
 * @see Scheduler
 */
final class PollableServiceProxy
    implements ReadyRunnable
{
    /**
     * interface that this service belongs to
     */
    private PollableService     _service;

    /**
     * the time (in milliseconds) after which this proxy
     * is supposed to run
     */
    private long                _scheduledRuntime;

    /**
     * Constructs a new instance of a proxy object that proxies
     * for the specified pollable service.
     *
     * @param psvc       The PollableService to be proxied.
     * @param runAt      The timestamp after which this proxy should run.
     */
    PollableServiceProxy(PollableService psvc, long runAt)
    {
        this._service = psvc;
        this._scheduledRuntime = runAt;
    }

    /**
     * Returns true if this proxy wants to be run.
     */
    public boolean isReady()
    {
        return (System.currentTimeMillis() > this.getScheduledRuntime());
    }
    
    /**
     * <P>This is the main method of the class.</P>
     *
     * <P>It passes a run() call through to the PollableService that it
     * proxies, and then makes a rescheduling decision on its behalf.  If
     * it encounters an inability to finish processing on run() (such as
     * a node lock unavailable or an interrupted thread) it will reschedule
     * itself to retry at 10 seconds.</P>
     *
     */
    public void run()
    {
        Category log = ThreadCategory.getInstance(getClass());

        // Check to see if the proxied service has caught up with the proxy
        if (System.currentTimeMillis() > this._service.getScheduledRuntime())
        {
            log.debug("run: Proxied service got ahead of proxy.  politely going away.");
            return; // Return and politely go away
        }
        
        try 
        {
            // Run the service, specifying that it should not reschedule itself
            log.debug("run: Proxy calling run() on the proxied service");
            this._service.run(false);

            // Calculate the interval and the next scheduled runtime
            long interval = this._service.recalculateInterval();
            this._scheduledRuntime = System.currentTimeMillis() + interval;
    
            /* If the next scheduled runtime is sooner than the one scheduled
             * for the proxied pollable, then go ahead and register it with
             * the scheduler.
             */
            if (this._scheduledRuntime < this._service.getScheduledRuntime())
            {
                log.debug("run: Proxy rescheduling itself at " + interval + " ms");
                Poller.getInstance().getScheduler().schedule(this, interval);
            }
            else
            {
                log.debug("run: Proxied service will run before the next scheduled"
                         +" time for the proxy.  Therefore, the proxy is politely"
                         +" going away");
            }
        } 
        catch (LockUnavailableException e)
        {
            log.debug("Lock unavailable; rescheduling at 10 seconds", e);
            Poller.getInstance().getScheduler().schedule(this, 10000);
        }
        catch (InterruptedException e)
        {
            log.debug("Thread Interrupted; rescheduling at 10 seconds", e);
            Poller.getInstance().getScheduler().schedule(this, 10000);
        }

        return;
    }    
    
    /**
     * Returns the time (in milliseconds) at which this Proxy is
     * scheduled to run.
     */
    private long getScheduledRuntime()
    {
        return this._scheduledRuntime;
    }
}
