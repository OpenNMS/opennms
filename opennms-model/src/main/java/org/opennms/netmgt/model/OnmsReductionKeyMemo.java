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
package org.opennms.netmgt.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Specific memo which is attached to every alarm with a matching reduction
 * key.</p>
 *
 * @author <a href="mailto:Markus@OpenNMS.com">Markus Neumann</a>
 */
@XmlRootElement(name = "reductionKeyMemo")
@Entity
@DiscriminatorValue(value="ReductionKeyMemo")
public class OnmsReductionKeyMemo extends OnmsMemo {

    private static final long serialVersionUID = 7472348439687562161L;

    @Column(name = "reductionkey")
    private String m_reductionKey;

    public String getReductionKey() {
        return m_reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.m_reductionKey = reductionKey;
    }
    
}