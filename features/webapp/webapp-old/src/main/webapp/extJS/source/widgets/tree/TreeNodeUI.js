/*
 * Ext JS Library 2.2.1
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 * @class Ext.tree.TreeNodeUI
 * This class provides the default UI implementation for Ext TreeNodes.
 * The TreeNode UI implementation is separate from the
 * tree implementation, and allows customizing of the appearance of
 * tree nodes.<br>
 * <p>
 * If you are customizing the Tree's user interface, you
 * may need to extend this class, but you should never need to instantiate this class.<br>
 * <p>
 * This class provides access to the user interface components of an Ext TreeNode, through
 * {@link Ext.tree.TreeNode#getUI}
 */
Ext.tree.TreeNodeUI = function(node){
    this.node = node;
    this.rendered = false;
    this.animating = false;
    this.wasLeaf = true;
    this.ecc = 'x-tree-ec-icon x-tree-elbow';
    this.emptyIcon = Ext.BLANK_IMAGE_URL;
};

Ext.tree.TreeNodeUI.prototype = {
    // private
    removeChild : function(node){
        if(this.rendered){
            this.ctNode.removeChild(node.ui.getEl());
        } 
    },

    // private
    beforeLoad : function(){
         this.addClass("x-tree-node-loading");
    },

    // private
    afterLoad : function(){
         this.removeClass("x-tree-node-loading");
    },

    // private
    onTextChange : function(node, text, oldText){
        if(this.rendered){
            this.textNode.innerHTML = text;
        }
    },

    // private
    onDisableChange : function(node, state){
        this.disabled = state;
		if (this.checkbox) {
			this.checkbox.disabled = state;
		}        
        if(state){
            this.addClass("x-tree-node-disabled");
        }else{
            this.removeClass("x-tree-node-disabled");
        } 
    },

    // private
    onSelectedChange : function(state){
        if(state){
            this.focus();
            this.addClass("x-tree-selected");
        }else{
            //this.blur();
            this.removeClass("x-tree-selected");
        }
    },

    // private
    onMove : function(tree, node, oldParent, newParent, index, refNode){
        this.childIndent = null;
        if(this.rendered){
            var targetNode = newParent.ui.getContainer();
            if(!targetNode){//target not rendered
                this.holder = document.createElement("div");
                this.holder.appendChild(this.wrap);
                return;
            }
            var insertBefore = refNode ? refNode.ui.getEl() : null;
            if(insertBefore){
                targetNode.insertBefore(this.wrap, insertBefore);
            }else{
                targetNode.appendChild(this.wrap);
            }
            this.node.renderIndent(true);
        }
    },

/**
 * Adds one or more CSS classes to the node's UI element.
 * Duplicate classes are automatically filtered out.
 * @param {String/Array} className The CSS class to add, or an array of classes
 */
    addClass : function(cls){
        if(this.elNode){
            Ext.fly(this.elNode).addClass(cls);
        }
    },

/**
 * Removes one or more CSS classes from the node's UI element.
 * @param {String/Array} className The CSS class to remove, or an array of classes
 */
    removeClass : function(cls){
        if(this.elNode){
            Ext.fly(this.elNode).removeClass(cls);  
        }
    },

    // private
    remove : function(){
        if(this.rendered){
            this.holder = document.createElement("div");
            this.holder.appendChild(this.wrap);
        }  
    },

    // private
    fireEvent : function(){
        return this.node.fireEvent.apply(this.node, arguments);  
    },

    // private
    initEvents : function(){
        this.node.on("move", this.onMove, this);

        if(this.node.disabled){
            this.addClass("x-tree-node-disabled");
			if (this.checkbox) {
				this.checkbox.disabled = true;
			}            
        }
        if(this.node.hidden){
            this.hide();
        }
        var ot = this.node.getOwnerTree();
        var dd = ot.enableDD || ot.enableDrag || ot.enableDrop;
        if(dd && (!this.node.isRoot || ot.rootVisible)){
            Ext.dd.Registry.register(this.elNode, {
                node: this.node,
                handles: this.getDDHandles(),
                isHandle: false
            });
        }
    },

    // private
    getDDHandles : function(){
        return [this.iconNode, this.textNode, this.elNode];
    },

/**
 * Hides this node.
 */
    hide : function(){
        this.node.hidden = true;
        if(this.wrap){
            this.wrap.style.display = "none";
        }
    },

/**
 * Shows this node.
 */
    show : function(){
        this.node.hidden = false;
        if(this.wrap){
            this.wrap.style.display = "";
        } 
    },

    // private
    onContextMenu : function(e){
        if (this.node.hasListener("contextmenu") || this.node.getOwnerTree().hasListener("contextmenu")) {
            e.preventDefault();
            this.focus();
            this.fireEvent("contextmenu", this.node, e);
        }
    },

    // private
    onClick : function(e){
        if(this.dropping){
            e.stopEvent();
            return;
        }
        if(this.fireEvent("beforeclick", this.node, e) !== false){
            var a = e.getTarget('a');
            if(!this.disabled && this.node.attributes.href && a){
                this.fireEvent("click", this.node, e);
                return;
            }else if(a && e.ctrlKey){
                e.stopEvent();
            }
            e.preventDefault();
            if(this.disabled){
                return;
            }

            if(this.node.attributes.singleClickExpand && !this.animating && this.node.isExpandable()){
                this.node.toggle();
            }

            this.fireEvent("click", this.node, e);
        }else{
            e.stopEvent();
        }
    },

    // private
    onDblClick : function(e){
        e.preventDefault();
        if(this.disabled){
            return;
        }
        if(this.checkbox){
            this.toggleCheck();
        }
        if(!this.animating && this.node.isExpandable()){
            this.node.toggle();
        }
        this.fireEvent("dblclick", this.node, e);
    },

    onOver : function(e){
        this.addClass('x-tree-node-over');
    },

    onOut : function(e){
        this.removeClass('x-tree-node-over');
    },

    // private
    onCheckChange : function(){
        var checked = this.checkbox.checked;
		// fix for IE6
		this.checkbox.defaultChecked = checked;
        this.node.attributes.checked = checked;
        this.fireEvent('checkchange', this.node, checked);
    },

    // private
    ecClick : function(e){
        if(!this.animating && this.node.isExpandable()){
            this.node.toggle();
        }
    },

    // private
    startDrop : function(){
        this.dropping = true;
    },
    
    // delayed drop so the click event doesn't get fired on a drop
    endDrop : function(){ 
       setTimeout(function(){
           this.dropping = false;
       }.createDelegate(this), 50); 
    },

    // private
    expand : function(){
        this.updateExpandIcon();
        this.ctNode.style.display = "";
    },

    // private
    focus : function(){
        if(!this.node.preventHScroll){
            try{this.anchor.focus();
            }catch(e){}
        }else{
            try{
                var noscroll = this.node.getOwnerTree().getTreeEl().dom;
                var l = noscroll.scrollLeft;
                this.anchor.focus();
                noscroll.scrollLeft = l;
            }catch(e){}
        }
    },

/**
 * Sets the checked status of the tree node to the passed value, or, if no value was passed,
 * toggles the checked status. If the node was rendered with no checkbox, this has no effect.
 * @param {Boolean} (optional) The new checked status.
 */
    toggleCheck : function(value){
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            this.onCheckChange();
        }
    },

    // private
    blur : function(){
        try{
            this.anchor.blur();
        }catch(e){} 
    },

    // private
    animExpand : function(callback){
        var ct = Ext.get(this.ctNode);
        ct.stopFx();
        if(!this.node.isExpandable()){
            this.updateExpandIcon();
            this.ctNode.style.display = "";
            Ext.callback(callback);
            return;
        }
        this.animating = true;
        this.updateExpandIcon();
        
        ct.slideIn('t', {
           callback : function(){
               this.animating = false;
               Ext.callback(callback);
            },
            scope: this,
            duration: this.node.ownerTree.duration || .25
        });
    },

    // private
    highlight : function(){
        var tree = this.node.getOwnerTree();
        Ext.fly(this.wrap).highlight(
            tree.hlColor || "C3DAF9",
            {endColor: tree.hlBaseColor}
        );
    },

    // private
    collapse : function(){
        this.updateExpandIcon();
        this.ctNode.style.display = "none";
    },

    // private
    animCollapse : function(callback){
        var ct = Ext.get(this.ctNode);
        ct.enableDisplayMode('block');
        ct.stopFx();

        this.animating = true;
        this.updateExpandIcon();

        ct.slideOut('t', {
            callback : function(){
               this.animating = false;
               Ext.callback(callback);
            },
            scope: this,
            duration: this.node.ownerTree.duration || .25
        });
    },

    // private
    getContainer : function(){
        return this.ctNode;  
    },

    // private
    getEl : function(){
        return this.wrap;  
    },

    // private
    appendDDGhost : function(ghostNode){
        ghostNode.appendChild(this.elNode.cloneNode(true));
    },

    // private
    getDDRepairXY : function(){
        return Ext.lib.Dom.getXY(this.iconNode);
    },

    // private
    onRender : function(){
        this.render();    
    },

    // private
    render : function(bulkRender){
        var n = this.node, a = n.attributes;
        var targetNode = n.parentNode ? 
              n.parentNode.ui.getContainer() : n.ownerTree.innerCt.dom;
        
        if(!this.rendered){
            this.rendered = true;

            this.renderElements(n, a, targetNode, bulkRender);

            if(a.qtip){
               if(this.textNode.setAttributeNS){
                   this.textNode.setAttributeNS("ext", "qtip", a.qtip);
                   if(a.qtipTitle){
                       this.textNode.setAttributeNS("ext", "qtitle", a.qtipTitle);
                   }
               }else{
                   this.textNode.setAttribute("ext:qtip", a.qtip);
                   if(a.qtipTitle){
                       this.textNode.setAttribute("ext:qtitle", a.qtipTitle);
                   }
               } 
            }else if(a.qtipCfg){
                a.qtipCfg.target = Ext.id(this.textNode);
                Ext.QuickTips.register(a.qtipCfg);
            }
            this.initEvents();
            if(!this.node.expanded){
                this.updateExpandIcon(true);
            }
        }else{
            if(bulkRender === true) {
                targetNode.appendChild(this.wrap);
            }
        }
    },

    // private
    renderElements : function(n, a, targetNode, bulkRender){
        // add some indent caching, this helps performance when rendering a large tree
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var cb = typeof a.checked == 'boolean';

        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = ['<li class="x-tree-node"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf x-unselectable ', a.cls,'" unselectable="on">',
            '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
            '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
            '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on" />',
            cb ? ('<input class="x-tree-node-cb" type="checkbox" ' + (a.checked ? 'checked="checked" />' : '/>')) : '',
            '<a hidefocus="on" class="x-tree-node-anchor" href="',href,'" tabIndex="1" ',
             a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '><span unselectable="on">',n.text,"</span></a></div>",
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>"].join('');

        var nel;
        if(bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }
        
        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        var index = 3;
        if(cb){
            this.checkbox = cs[3];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;			
            index++;
        }
        this.anchor = cs[index];
        this.textNode = cs[index].firstChild;
    },

/**
 * Returns the &lt;a> element that provides focus for the node's UI.
 * @return {HtmlElement} The DOM anchor element.
 */
    getAnchor : function(){
        return this.anchor;
    },
    
/**
 * Returns the text node.
 * @return {HtmlNode} The DOM text node.
 */
    getTextEl : function(){
        return this.textNode;
    },
    
/**
 * Returns the icon &lt;img> element.
 * @return {HtmlElement} The DOM image element.
 */
    getIconEl : function(){
        return this.iconNode;
    },

/**
 * Returns the checked status of the node. If the node was rendered with no
 * checkbox, it returns false.
 * @return {Boolean} The checked flag.
 */
    isChecked : function(){
        return this.checkbox ? this.checkbox.checked : false; 
    },

    // private
    updateExpandIcon : function(){
        if(this.rendered){
            var n = this.node, c1, c2;
            var cls = n.isLast() ? "x-tree-elbow-end" : "x-tree-elbow";
            if(n.isExpandable()){
                if(n.expanded){
                    cls += "-minus";
                    c1 = "x-tree-node-collapsed";
                    c2 = "x-tree-node-expanded";
                }else{
                    cls += "-plus";
                    c1 = "x-tree-node-expanded";
                    c2 = "x-tree-node-collapsed";
                }
                if(this.wasLeaf){
                    this.removeClass("x-tree-node-leaf");
                    this.wasLeaf = false;
                }
                if(this.c1 != c1 || this.c2 != c2){
                    Ext.fly(this.elNode).replaceClass(c1, c2);
                    this.c1 = c1; this.c2 = c2;
                }
            }else{
                if(!this.wasLeaf){
                    Ext.fly(this.elNode).replaceClass("x-tree-node-expanded", "x-tree-node-leaf");
                    delete this.c1;
                    delete this.c2;
                    this.wasLeaf = true;
                }
            }
            var ecc = "x-tree-ec-icon "+cls;
            if(this.ecc != ecc){
                this.ecNode.className = ecc;
                this.ecc = ecc;
            }
        }
    },

    // private
    getChildIndent : function(){
        if(!this.childIndent){
            var buf = [];
            var p = this.node;
            while(p){
                if(!p.isRoot || (p.isRoot && p.ownerTree.rootVisible)){
                    if(!p.isLast()) {
                        buf.unshift('<img src="'+this.emptyIcon+'" class="x-tree-elbow-line" />');
                    } else {
                        buf.unshift('<img src="'+this.emptyIcon+'" class="x-tree-icon" />');
                    }
                }
                p = p.parentNode;
            }
            this.childIndent = buf.join("");
        }
        return this.childIndent;
    },

    // private
    renderIndent : function(){
        if(this.rendered){
            var indent = "";
            var p = this.node.parentNode;
            if(p){
                indent = p.ui.getChildIndent();
            }
            if(this.indentMarkup != indent){ // don't rerender if not required
                this.indentNode.innerHTML = indent;
                this.indentMarkup = indent;
            }
            this.updateExpandIcon();
        }
    },

    destroy : function(){
        if(this.elNode){
            Ext.dd.Registry.unregister(this.elNode.id);
        }
        delete this.elNode;
        delete this.ctNode;
        delete this.indentNode;
        delete this.ecNode;
        delete this.iconNode;
        delete this.checkbox;
        delete this.anchor;
        delete this.textNode;
        
        if (this.holder){
             delete this.wrap;
             Ext.removeNode(this.holder);
             delete this.holder;
        }else{
            Ext.removeNode(this.wrap);
            delete this.wrap;
        }
    }
};

/**
 * @class Ext.tree.RootTreeNodeUI
 * This class provides the default UI implementation for <b>root</b> Ext TreeNodes.
 * The RootTreeNode UI implementation allows customizing the appearance of the root tree node.<br>
 * <p>
 * If you are customizing the Tree's user interface, you
 * may need to extend this class, but you should never need to instantiate this class.<br>
 */
Ext.tree.RootTreeNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    // private
    render : function(){
        if(!this.rendered){
            var targetNode = this.node.ownerTree.innerCt.dom;
            this.node.expanded = true;
            targetNode.innerHTML = '<div class="x-tree-root-node"></div>';
            this.wrap = this.ctNode = targetNode.firstChild;
        }
    },
    collapse : Ext.emptyFn,
    expand : Ext.emptyFn
});