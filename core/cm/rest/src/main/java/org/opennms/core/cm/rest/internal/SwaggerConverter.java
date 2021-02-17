package org.opennms.core.cm.rest.internal;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaVisitor;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;

import io.swagger.v3.oas.models.OpenAPI;

public class SwaggerConverter implements XmlSchemaVisitor {

    private OpenAPI openAPI = new OpenAPI();

    public OpenAPI convert(XmlSchemaCollection collection) {
        XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
        walker.walk(getElementOf(collection, "VacuumdConfiguration"));
        return openAPI;
    }

    private static XmlSchemaElement getElementOf(XmlSchemaCollection collection, String name) {
        XmlSchemaElement elem = null;
        for (XmlSchema schema : collection.getXmlSchemas()) {
            elem = schema.getElementByName(name);
            if (elem != null) {
                break;
            }
        }
        return elem;
    }

    @Override
    public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        System.out.println("onEnterElement(" + xmlSchemaElement.getQName() + ")");
    }

    @Override
    public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        System.out.println("onExitElement(" + xmlSchemaElement.getQName() + ")");
    }

    @Override
    public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        System.out.println("onVisitAttribute(" + xmlSchemaElement.getQName() + ")");
    }

    @Override
    public void onEndAttributes(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo) {
        System.out.println("onEndAttributes(" + xmlSchemaElement.getQName() + ")");
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
