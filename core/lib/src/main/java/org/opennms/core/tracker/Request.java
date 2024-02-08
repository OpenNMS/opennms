/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
