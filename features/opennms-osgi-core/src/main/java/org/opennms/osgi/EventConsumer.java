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
package org.opennms.osgi;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If you want an instance of a class to get notified when an event of a concrete type is fired, just add
 * this annotation to the method which consumes that event object. <br/><br/>
 * <b>Example:</b><br/>
 * <pre>
 *     // An event consumer
 *     class MyEventConsumer {
 *         //...
 *        &#64;EventConsumer
 *        public void eventTypeChanged(EventType eventType) {
 *            // do something..
 *        }
 *         //...
 *     }
 *
 *     // some event producer which publishes the event through the EventRegistry
 *     EventProducer producer = new EventProducer();
 *     producer.fireEvent(new EventType());
 * </pre>
 *
 * @see {@link EventRegistry}, {@link EventProxy}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventConsumer {

}
