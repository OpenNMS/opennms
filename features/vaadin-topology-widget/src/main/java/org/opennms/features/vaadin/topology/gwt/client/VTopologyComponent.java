package org.opennms.features.vaadin.topology.gwt.client;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VTopologyComponent extends Composite implements Paintable {

    private static VTopologyComponentUiBinder uiBinder = GWT
            .create(VTopologyComponentUiBinder.class);

    interface VTopologyComponentUiBinder extends
            UiBinder<Widget, VTopologyComponent> {
    }
    
    private ApplicationConnection m_client;
    private String m_paintableId;
    private D3 m_svg;
    private int m_width;
    private int m_height;
    
    public VTopologyComponent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        
        
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        D3 d3 = getD3();
        m_width = 300;
        m_height = 200;
        m_svg = d3.select("#chart-2").append("svg").attr("width", 300).attr("height", 200);
        
//        m_runButton.addClickHandler(new ClickHandler() {
//
//            public void onClick(ClickEvent event) {
//                circle.style("fill", "#aaa").attr("r", 12).attr("cy", y);
//                circle.transition().duration(500).delay(0).style("fill", "steelBlue");
//                circle.transition().duration(500).delay(500).attr("cy", 90);
//                circle.transition().duration(500).delay(1000).attr("r", 30);
//            }
//            
//        });
    }

    private void drawCircles(int[] data) {

    	Window.alert("Received data " + data);


    	JavaScriptObject x = getD3().scale().ordinal().domain(data).rangePoints(rangeArray(m_width), 1);
    	final JavaScriptObject y = getD3().scale().ordinal().domain(data).rangePoints(rangeArray(m_height), 1);


    	final D3 circle = m_svg.selectAll(".little")
    			.data(data)
    			.enter()
    			.append("circle")
    			.attr("class", "little")
    			.attr("cx", x)
    			.attr("cy", y)
    			.attr("r", 12);

    	circle.style("fill", "#aaa").attr("r", 12).attr("cy", y);
    	circle.transition().duration(500).delay(0).style("fill", "steelBlue");
    	circle.transition().duration(500).delay(500).attr("cy", 90);
    	circle.transition().duration(500).delay(1000).attr("r", 30);
    }
    
    private final native void drawCirclesJS(int[] data, JavaScriptObject svg, int w, int h) /*-{
    
    
        var d3 =$wnd.d3;
    	var x = d3.scale.ordinal().domain(data).rangePoints([0, w], 1);
    	var y = d3.scale.ordinal().domain(data).rangePoints([0, h], 2);

    	var circle = svg.selectAll(".little").data(data);
    			
    	circle.enter()
    			.append("circle")
    			.attr("class", "little")
    			.attr("cx", x)
    			.attr("cy", y)
    			.attr("r", 0);

    	circle.style("fill", "#aaa")
    	circle.exit().transition().duration(1000).delay(0).attr("cx", x).attr("r", 0).remove();
    	circle.transition().duration(1000).delay(0).attr("r", 12).attr("cx", x).attr("cy", y);
    	circle.transition().duration(500).delay(1500).style("fill", "steelBlue");
    	circle.transition().duration(500).delay(2000).attr("cy", 90);
    	circle.transition().duration(500).delay(2500).attr("r", 30);
    }-*/;
    
    
    private static native JsArray getTestArray() /*-{
        return [32, 55, 112];
    }-*/;
    
    private static native JsArray rangeArray(int range) /*-{
        return [0, range];
    }-*/;
    
    private native D3 getD3() /*-{
        return $wnd.d3;
    }-*/;
    

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        
        if(client.updateComponent(this, uidl, true)) {
            
            return;
        }
        
        m_client = client;
        m_paintableId = uidl.getId();
        
        drawCirclesJS(uidl.getIntArrayAttribute("dataArray"), m_svg, m_width, m_height);
    }

}
