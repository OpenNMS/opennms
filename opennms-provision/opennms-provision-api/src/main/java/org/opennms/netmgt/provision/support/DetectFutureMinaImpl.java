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
package org.opennms.netmgt.provision.support;

import java.util.Map;

import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;

/**
 * <p>DefaultDetectFuture class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class DetectFutureMinaImpl extends DefaultIoFuture implements DetectFuture {
    
    private final AsyncServiceDetector m_detector;

    /**
     * <p>Constructor for DefaultDetectFuture.</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public DetectFutureMinaImpl(final AsyncServiceDetector detector) {
        super(null);
        m_detector = detector;
    }

    /**
     * <p>isServiceDetected</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isServiceDetected() {
        return Boolean.TRUE.equals(getValue());
    }

    /**
     * <p>getServiceAttributes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, String> getServiceAttributes() {
        return null;
    }

    /**
     * <p>getException</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    @Override
    public Throwable getException() {
        final Object val = getValue();
        if (val instanceof Throwable) {
            return (Throwable)val;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceDetected(final boolean serviceDetected) {
        setValue(serviceDetected);
    }

    /** {@inheritDoc} */
    @Override
    public void setException(final Throwable throwable) {
//        System.err.println("setting exception to " + throwable);
        setValue(throwable);
    }

    /**
     * <p>getObjectValue</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getObjectValue() {
        return super.getValue();
    }

    @Override
    public void awaitFor() throws InterruptedException {
        super.await();
    }

    @Override
    public void awaitForUninterruptibly() {
        super.awaitUninterruptibly();
    }

    @Override
    public DetectFuture addListener(final DetectFutureListener<DetectFuture> listener) {
        super.addListener(new IoFutureListener<DetectFutureMinaImpl>() {

            @Override
            public void operationComplete(DetectFutureMinaImpl future) {
                listener.operationComplete(future);
            }

        });
        return this;
    }
}
