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
package org.opennms.features.config.dao.impl.util;

import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaVisitor;

public class NoopXmlSchemaVisitor implements XmlSchemaVisitor {

    @Override
    public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {

    }

    @Override
    public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {

    }

    @Override
    public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {

    }

    @Override
    public void onEndAttributes(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo) {

    }

    @Override
    public void onEnterSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {

    }

    @Override
    public void onExitSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {

    }

    @Override
    public void onEnterAllGroup(XmlSchemaAll xmlSchemaAll) {

    }

    @Override
    public void onExitAllGroup(XmlSchemaAll xmlSchemaAll) {

    }

    @Override
    public void onEnterChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {

    }

    @Override
    public void onExitChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {

    }

    @Override
    public void onEnterSequenceGroup(XmlSchemaSequence xmlSchemaSequence) {

    }

    @Override
    public void onExitSequenceGroup(XmlSchemaSequence xmlSchemaSequence) {

    }

    @Override
    public void onVisitAny(XmlSchemaAny xmlSchemaAny) {

    }

    @Override
    public void onVisitAnyAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAnyAttribute xmlSchemaAnyAttribute) {

    }
}
