/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.tracker;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A Request intended to be sent via a Messenger. This interfaces only
 * provides for retrieving the id for the request so it can be matched against
 * any replies. It also has methods that are using to indicate if an error has
 * occurred or a timeout has occured.
 *
 * @author brozow
 */
public interface Request<RequestIdT, RequestT extends Request<RequestIdT, RequestT, ResponseT>, ResponseT> extends Delayed {

    /**
     * Returns the id of this request. This is is matched against the id of a
     * reply in order to associate replies with requests.
     */
    RequestIdT getId();

    /**
     * Indicates how many units of time are remaining until this request times
     * out. Please note that this is the time REMAINING not the total timeout
     * time.
     *
     * This method is inherited from Delayed
     */
    long getDelay(TimeUnit unit);

    /**
     * Tell the request about a reply that come in with matched ids. Further
     * processing is left to the request.
     */
    boolean processResponse(ResponseT reply);

    /**
     * Notify this request that no reply has come in before its timeout has
     * elapsed. (The timeout is indiciated using the getDelay method). If a
     * retry should be attempted then a new request should be returned that
     * can be retried, otherwise null should be returned.
     */
    RequestT processTimeout();

    /**
     * If an error or exception occurs during processing of this request then
     * processError is called with the error or exception object.
     */
    void processError(Throwable t);

        /**
         * Returns true if this request has already been processed.
         *
         * This method should return true if and only if one of the process method
         * has been called.
         */
        boolean isProcessed();


}
