/*
 * Ext JS Library 2.2.1
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 @class Ext.util.ClickRepeater
 @extends Ext.util.Observable

 A wrapper class which can be applied to any element. Fires a "click" event while the
 mouse is pressed. The interval between firings may be specified in the config but
 defaults to 20 milliseconds.

 Optionally, a CSS class may be applied to the element during the time it is pressed.

 @cfg {Mixed} el The element to act as a button.
 @cfg {Number} delay The initial delay before the repeating event begins firing.
 Similar to an autorepeat key delay.
 @cfg {Number} interval The interval between firings of the "click" event. Default 20 ms.
 @cfg {String} pressClass A CSS class name to be applied to the element while pressed.
 @cfg {Boolean} accelerate True if autorepeating should start slowly and accelerate.
           "interval" and "delay" are ignored.
 @cfg {Boolean} preventDefault True to prevent the default click event
 @cfg {Boolean} stopDefault True to stop the default click event

 @history
    2007-02-02 jvs Original code contributed by Nige "Animal" White
    2007-02-02 jvs Renamed to ClickRepeater
    2007-02-03 jvs Modifications for FF Mac and Safari

 @constructor
 @param {Mixed} el The element to listen on
 @param {Object} config
 */
Ext.util.ClickRepeater = function(el, config)
{
    this.el = Ext.get(el);
    this.el.unselectable();

    Ext.apply(this, config);

    this.addEvents(
    /**
     * @event mousedown
     * Fires when the mouse button is depressed.
     * @param {Ext.util.ClickRepeater} this
     */
        "mousedown",
    /**
     * @event click
     * Fires on a specified interval during the time the element is pressed.
     * @param {Ext.util.ClickRepeater} this
     */
        "click",
    /**
     * @event mouseup
     * Fires when the mouse key is released.
     * @param {Ext.util.ClickRepeater} this
     */
        "mouseup"
    );

    this.el.on("mousedown", this.handleMouseDown, this);
    if(this.preventDefault || this.stopDefault){
        this.el.on("click", function(e){
            if(this.preventDefault){
                e.preventDefault();
            }
            if(this.stopDefault){
                e.stopEvent();
            }
        }, this);
    }

    // allow inline handler
    if(this.handler){
        this.on("click", this.handler,  this.scope || this);
    }

    Ext.util.ClickRepeater.superclass.constructor.call(this);
};

Ext.extend(Ext.util.ClickRepeater, Ext.util.Observable, {
    interval : 20,
    delay: 250,
    preventDefault : true,
    stopDefault : false,
    timer : 0,

    // private
    destroy : function() {
        Ext.destroy(this.el);
        this.purgeListeners();
    },
    
    // private
    handleMouseDown : function(){
        clearTimeout(this.timer);
        this.el.blur();
        if(this.pressClass){
            this.el.addClass(this.pressClass);
        }
        this.mousedownTime = new Date();

        Ext.getDoc().on("mouseup", this.handleMouseUp, this);
        this.el.on("mouseout", this.handleMouseOut, this);

        this.fireEvent("mousedown", this);
        this.fireEvent("click", this);

//      Do not honor delay or interval if acceleration wanted.
        if (this.accelerate) {
            this.delay = 400;
	    }
        this.timer = this.click.defer(this.delay || this.interval, this);
    },

    // private
    click : function(){
        this.fireEvent("click", this);
        this.timer = this.click.defer(this.accelerate ?
            this.easeOutExpo(this.mousedownTime.getElapsed(),
                400,
                -390,
                12000) :
            this.interval, this);
    },

    easeOutExpo : function (t, b, c, d) {
        return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
    },

    // private
    handleMouseOut : function(){
        clearTimeout(this.timer);
        if(this.pressClass){
            this.el.removeClass(this.pressClass);
        }
        this.el.on("mouseover", this.handleMouseReturn, this);
    },

    // private
    handleMouseReturn : function(){
        this.el.un("mouseover", this.handleMouseReturn, this);
        if(this.pressClass){
            this.el.addClass(this.pressClass);
        }
        this.click();
    },

    // private
    handleMouseUp : function(){
        clearTimeout(this.timer);
        this.el.un("mouseover", this.handleMouseReturn, this);
        this.el.un("mouseout", this.handleMouseOut, this);
        Ext.getDoc().un("mouseup", this.handleMouseUp, this);
        this.el.removeClass(this.pressClass);
        this.fireEvent("mouseup", this);
    }
});