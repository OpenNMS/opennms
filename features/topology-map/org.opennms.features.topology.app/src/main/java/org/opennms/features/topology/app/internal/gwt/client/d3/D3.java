/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client.d3;

import org.opennms.features.topology.app.internal.gwt.client.GWTBoundingBox;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.XMLHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

public class D3 extends JavaScriptObject {
    
    protected D3() {};
    
    public final native D3 select(String elementId) /*-{
        return this.select(elementId);
    }-*/;
    
    public final native Element selectElement(String selector) /*-{
        var retElement = this.select(selector);
        if(retElement.length > 0){
            return retElement[0][0];
        }
        
        return null;
    }-*/;
    
    public static final native Element getElement(D3 selection, int index) /*-{
        return selection[0][index];
    }-*/;
    
    public final native D3 select(Element elem) /*-{
        return this.select(elem);
    }-*/;
    
    public final native D3 append(String tagName) /*-{
        return this.append(tagName);
    }-*/;
    
    public final native D3 attr(String propName, int value) /*-{
        return this.attr(propName, value);
    }-*/;
    
    public final native D3 attr(String propName, JavaScriptObject func) /*-{
        return this.attr(propName, func);
    }-*/;
    
    public final native D3 attr(String attrName, Func<?,?> func) /*-{
		var f = function(d, i){
			return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
		}
		return this.attr(attrName, f);
    }-*/;
    
    public final native D3 attrTween(String attrName, Func<?, ?>func) /*-{
        var f = function(d, i){
            return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
        }
        return this.attrTween(attrName, f);
    }-*/;
    
    public final native D3 attrTweenZoom(String string, GWTBoundingBox boundingBox,GWTBoundingBox oldBBox, int width, int height) /*-{
        var width = 1000,
            height = 600;
        
        function matchAspect(p){
            var R = width/height;
            var r = p[2]/p[3];
            return [
                    p[0],
                    p[1],
                    r < R ? p[3]*R : p[2],
                    r < R ? p[3] : p[2]/R
                   ];
        }
        
        function transform(p) {
            var k = width / p[2];
            var retVal = "translate(" + (center[0] - p[0] * k) + "," + (center[1] - p[1] * k) + ")scale(" + k + ")";
            $wnd.console.log(retVal);
            return retVal;
        }
        
        var p0 = [250, 200, 30, 90],
            p1 = [560, 300, 200, 120];
        
        var s = [oldBBox.width/2 + oldBBox.x, oldBBox.height/2 + oldBBox.y, oldBBox.width, oldBBox.height];
        var e = [boundingBox.width/2 + boundingBox.x, boundingBox.height/2 + boundingBox.y, boundingBox.width, boundingBox.height];
        $wnd.console.log("s: " + s);
        $wnd.console.log("e: " + e);
        var start = matchAspect(s);
        var end = matchAspect(e);
        var center = [width / 2, height / 2],
              i = $wnd.d3.interpolateZoom(start, end);
        $wnd.console.log("start: " + start);
        $wnd.console.log("end: " + end);
        $wnd.console.log(i)
        
        return this.attr("transform", transform(start)).transition().duration(i.duration * 2).attrTween(string, function(){ function(t){ return transform(i(t))} });
    }-*/;
    
    public final native void html(Func<?, ?> func) /*-{
        var f = function(d, i){
            return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
        }
        return this.html(f);
        
    }-*/;
    
    public final native void html(String html) /*-{
        this.html(html);
    }-*/;
    
    public final native void zoomTransition(D3 selection, int width, int height, JsArrayInteger p0, JsArrayInteger p1) /*-{
		transition(p0, p1);
		
		function matchAspect(p) {
			var R = width/height;
			var r = p[2]/p[3];
			return [
			        p[0],
			        p[1],
			        r < R ? p[3]*R : p[2],
			        r < R ? p[3] : p[2]/R
			       ];
		}
		     
		
		function transition(s, e) {
		  var start = matchAspect(s);
		  var end = matchAspect(e);
		  var center = [width / 2, height / 2],
		      i = $wnd.d3.interpolateZoom(start, end);
		  
		  selection
		      .attr("transform", transform(start))
		    .transition()
		      .delay(250)
		      .duration(i.duration * 2)
		      .attrTween("transform", function() { return function(t) { return transform(i(t)); }; })
		      //.each("end", function() { $wnd.d3.select(this).call(transition, e, s); });
		
		  function transform(p) {
		    var k = width / p[2];
		    var retVal = "translate(" + (center[0] - p[0] * k) + "," + (center[1] - p[1] * k) + ")scale(" + k + ")";
		    return retVal;
		  }
		}
    }-*/;
    
    public final native void timer(BooleanFunc func) /*-{
        var f = function(){
            return func.@org.opennms.features.topology.app.internal.gwt.client.d3.BooleanFunc::call()();
        }
        $wnd.d3.timer(f);
    }-*/;
    
    public final native void timer() /*-{
        $wnd.d3.timer(function() {
            $wnd.console.log("timer tick");
            return false;
            
        })
    }-*/;

    public final native D3 selectAll(String selectionName) /*-{
        return this.selectAll(selectionName);
    }-*/;
    
    public final native D3 data(JsArray<?> array) /*-{
        return this.data(array);
    }-*/;

    public final native D3 enter() /*-{
        return this.enter();
    }-*/;
    
    public final native D3 update() /*-{
        return this.update();
    }-*/;

    public final native D3 attr(String propName, String value) /*-{
        return this.attr(propName, value);
    }-*/;
    
    public final native D3 attr(String propName, double value) /*-{
        return this.attr(propName, value);
    }-*/;
    
    public final native D3Scale scale() /*-{
        return this.scale;
    }-*/;

    public final native D3 style(String styleName, String value) /*-{
        return this.style(styleName, value);
    }-*/;

    public final native D3 transition() /*-{
        return this.transition();
    }-*/;
    
    public final native D3 styleTween(String name, Tween<?, ?> t) /*-{
        
        function interpolate(a, b) {
          var numA = a.replace('px', '');
          var numB = b.replace('px', '');
          numB -= numA;
          return function(t) { $wnd.console.log("numA: " + numA + " numB: " + numB + " t: " + t); return (numA + numB * t) + "px"; };
        };
        
        function tween(d, i, a){
            var f = $wnd.d3.interpolate(a,"11px");
            $wnd.console.log("f: ");
            $wnd.console.log(f);
            return function(t){ var ret = f(t); $wnd.console.log("t: " + t + " ret: " + ret); return ret; }
            //return $wnd.d3.interpolate(a, t.@org.opennms.features.topology.app.internal.gwt.client.d3.Tween::call(Ljava/lang/Object;ILjava/lang/String;)(d,i,a));
        }
        return this.styleTween(name, tween);
    }-*/;

    public final native D3 duration(int duration) /*-{
        return this.duration(duration);
    }-*/;

    public final native D3 delay(int delayInMilliSeconds) /*-{
        return this.delay(delayInMilliSeconds);
    }-*/;

    public final native D3 exit() /*-{
        return this.exit();
    }-*/;

    public final native D3 remove() /*-{
        return this.remove();
    }-*/;

    public final native <T extends JavaScriptObject> D3 data(JsArray<T> data, Func<?, T> func) /*-{
    	var f = function(d, i) {
        	return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
        };
        return this.data(data, f);
		
    }-*/;

    public final native D3 text(JavaScriptObject textFunc) /*-{
        return this.text(textFunc);
    }-*/;
    
    public final native D3 text(String t) /*-{
        return this.text(t);
    }-*/;

	public final native D3 text(Func<String, ?> func) /*-{
	   var f = function(d, i){
		   return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
	   }
	   return this.text(f);
	
    }-*/;
	
	public final native void each(Handler<?> handler) /*-{
	    var f = function(d, i){
	        return handler.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
	    }
	    return this.each(f);
	}-*/;
	
	/**
	 * Only used for transitions
	 * 
	 * @param type
	 * @param handler
	 */
	public final native void each(String type, AnonymousFunc func) /*-{
        var f = function(){
            func.@org.opennms.features.topology.app.internal.gwt.client.d3.AnonymousFunc::call()();
        }
        
        return this.each(type, f);
    }-*/;

	public final native D3 on(String event, Handler<?> handler) /*-{
	   	var f = function(d, i) {
	   		return handler.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
	   	}
	
		return this.on(event, f);
	}-*/;

	public final native D3 style(String styleName, Func<String, ?> func) /*-{
		var f = function(d, i){
			return func.@org.opennms.features.topology.app.internal.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
		}
		return this.style(styleName, f);
		
	}-*/;

    public static final native NativeEvent getEvent() /*-{
        if(typeof($wnd.d3.event.sourceEvent) != "undefined"){
            return $wnd.d3.event.sourceEvent;
        }
        return $wnd.d3.event;
    }-*/;
    
    public static final native void eventPreventDefault() /*-{
        $wnd.d3.event.preventDefault();
    }-*/;
    
    public static final native D3 d3() /*-{
        return $wnd.d3;
    }-*/;
    
    public static final native JavaScriptObject property(String propertName) /*-{
        return function(d,i){
            return d[propertName];
        };
    }-*/;
    
    public static final native D3Drag getDragBehavior() /*-{
        return $wnd.d3.behavior.drag();
    }-*/;

    public static final native JavaScriptObject drag() /*-{
        
        var drag = $wnd.d3.behavior.drag();
        
            
        drag.on("dragstart", function(d,i){ 
        });
        
        //drag.on("drag", function(d,i){ console.log("drag") });
        
        drag.on("dragend", function(d,i){ console.log("dragend :: event: " + $wnd.d3.event) });
        
        
        return drag;
     }-*/;

    public final native D3 call(JavaScriptObject behavior) /*-{
        return this.call(behavior);
    }-*/;
    
    /**
     * Takes a D3Behavior and returns the current selection passed into the run method
     * @param behavior
     * @return
     */
    public final native D3 call(D3Behavior behavior) /*-{
        behavior.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior::run(Lorg/opennms/features/topology/app/internal/gwt/client/d3/D3;)(this);
        return this;
    }-*/;
    
    /**
     * Takes a D3Behavior and returns the resulting selection
     * @param behavior
     * @return
     */
    public final native D3 with(D3Behavior behavior) /*-{
        return behavior.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior::run(Lorg/opennms/features/topology/app/internal/gwt/client/d3/D3;)(this);
    }-*/;
    
    /**
     * Create is intended to be used with enter methods. 
     * And the behavior is expected to return the created selection
     * @param behavior
     * @return
     */
    public final D3 create(D3Behavior behavior) {
        return with(behavior);
    };

	public static final native JsArrayInteger getMouse(Element elem) /*-{
		return $wnd.d3.mouse(elem);
		
	}-*/;

	public static final native JsArrayNumber getMouse(D3 select) /*-{
		return $wnd.d3.mouse(select);
	}-*/;

	public final native String attr(String property) /*-{
		return this.attr(property);
	}-*/;

	public static final native D3Transform getTransform(String transform)/*-{
	    return $wnd.d3.transform(transform);
	}-*/;

    public final native D3Brush getBrush() /*-{
        return this.svg.brush();
    }-*/;

    public final native void xml(String iconUrl, String mimeType, XMLHandler<?> handler) /*-{
        var f = function(d) {
            return handler.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.XMLHandler::call(Ljava/lang/Object;)(d);
        }
        this.xml(iconUrl, mimeType, f);
    }-*/;

    public final native int length() /*-{
        return this.data().length;
        
    }-*/;

    public final native String style(String style) /*-{
        return this.style(style);
    }-*/;


}
