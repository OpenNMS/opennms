package org.opennms.features.topology.plugins.ncs.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;

public class ShowNCSPathOperation implements Operation {

    @Override
    public Undoer execute(List<VertexRef> targets, final OperationContext operationContext) {
        
        final Window mainWindow = operationContext.getMainWindow();
        
        final Window ncsPathPrompt = new Window();
        ncsPathPrompt.isModal();
        ncsPathPrompt.setResizable(false);
        ncsPathPrompt.setWidth("300px");
        ncsPathPrompt.setHeight("220px");
        
        //Items used in form field
        final PropertysetItem item = new PropertysetItem();
        item.addItemProperty("Point A", new ObjectProperty<String>("", String.class));
        item.addItemProperty("Point B", new ObjectProperty<String>("", String.class));
        
        final Form promptForm = new Form() {

            @Override
            public void commit() throws SourceException, InvalidValueException {
                String pointA = (String) getField("Point A").getValue();
                String pointB = (String) getField("Point B").getValue();
                
                if(pointA == null || pointB == null) {
                    throw new InvalidValueException("Point A and Point B cannot be null.");
                }
                getField("Point A").setValue(pointA.trim());
                getField("Point B").setValue(pointB.trim());
                super.commit();
                
                pointA = (String) getField("Point A").getValue();
                pointB = (String) getField("Point B").getValue();
                
                VertexRef vertexA = operationContext.getGraphContainer().getBaseTopology().getVertex("node", pointA);
                VertexRef vertexB = operationContext.getGraphContainer().getBaseTopology().getVertex("node", pointB);
                
                
            }
            
        };
        
        promptForm.setWriteThrough(false);
        promptForm.setItemDataSource(item);
        Field fieldA = promptForm.getField("Point A");
        fieldA.setRequired(true);
        fieldA.setRequiredError("Point A cannot be blank");
        
        Field fieldB = promptForm.getField("Point B");
        fieldB.setRequired(true);
        fieldB.setRequiredError("Point B cannot be blank");
        
        Button ok = new Button("OK;");
        ok.addListener(new ClickListener() {

            private static final long serialVersionUID = -2742886456007926688L;

            @Override
            public void buttonClick(ClickEvent event) {
                promptForm.commit();
                mainWindow.removeWindow(ncsPathPrompt);
            }
            
        });
        promptForm.getFooter().addComponent(ok);
        
        Button cancel = new Button("Cancel");
        cancel.addListener(new ClickListener(){
            private static final long serialVersionUID = -9026067481179449095L;

            @Override
            public void buttonClick(ClickEvent event) {
                mainWindow.removeWindow(ncsPathPrompt);
            }
            
        });
        
        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return targets.size() == 1 && targets.get(0).getNamespace().equals("node");
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        return targets.size() == 1 && targets.get(0).getNamespace().equals("node");
    }

    @Override
    public String getId() {
        return null;
    }

}
