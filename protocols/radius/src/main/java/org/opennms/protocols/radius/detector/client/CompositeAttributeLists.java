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
package org.opennms.protocols.radius.detector.client;

import net.jradius.client.auth.EAPTTLSAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;

 /**
  * @author JMK <jm+opennms@kubek.fr>
  *
  */
 public class CompositeAttributeLists {

     private AttributeList m_outerAttributes;
     private AttributeList m_innerAttributes;
     private String m_innerAuthType;
     private Boolean m_trustAll = true;

     public CompositeAttributeLists(AttributeList attributes) {
         m_outerAttributes = attributes;
     }

     public void addToInner(RadiusAttribute attribute) {
         if (hasNoInnerAttributes()){
             setInnerAttributes(new AttributeList());
         }
         m_innerAttributes.add(attribute);
     }

     private void setInnerAttributes(AttributeList attributeList) {
         m_innerAttributes = attributeList;

     }

     public boolean hasNoInnerAttributes() {
         return (m_innerAttributes == null);
     }

     public void setTunneledAuthType(String ttlsInnerAuthType) {
         m_innerAuthType = ttlsInnerAuthType;
     }

     public AccessRequest createRadiusRequest(RadiusAuthenticator radiusAuthenticator) {
         AccessRequest request = new AccessRequest();
         request.addAttributes(m_outerAttributes);
         if (radiusAuthenticator instanceof EAPTTLSAuthenticator){
             ((EAPTTLSAuthenticator) radiusAuthenticator).setTunneledAttributes(m_innerAttributes);
             ((EAPTTLSAuthenticator) radiusAuthenticator).setInnerProtocol(m_innerAuthType);
             ((EAPTTLSAuthenticator) radiusAuthenticator).setTrustAll(m_trustAll);
         }

         return request;
     }

 }
