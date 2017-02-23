(function ($) {
    "use strict";

    var options = {
    };

    function callContextMenu(plot, e) {
        var ctxMenu = $('<div id="contextMenu"/>').html('<table  border="0" cellpadding="0" cellspacing="0" style="border: thin solid #808080; cursor: default;" width="100px"><tr><td><div class="ContextItem" style="cursor:pointer;background:#ffffff; color: black;border-radius:5px;font-size:0.9em; padding: 5px;">Save image...</div></td></tr></table>');
        var posx = e.clientX +window.pageXOffset +'px'; // Left Position of Mouse Pointer
        var posy = e.clientY + window.pageYOffset -35+ 'px'; // Top Position of Mouse
        ctxMenu.css({
            'position': 'absolute',
            'left': posx,
            'top': posy
        });
        $('body').append(ctxMenu);
        $(ctxMenu)
            .click(function(){
                savePNG(plot);
                closeContextMenu();
            });
        return false;
    }

    function closeContextMenu() {
        var t = $('#contextMenu');
        if (t) {
            $(t).remove();
        }
    }

    function savePNG(plot) {
        var dataString = plot.getCanvas().toDataURL("image/png");
        window.open(dataString.replace("image/png", "image/octet-stream"));
    }

    function init(plot) {
        plot.hooks.processOptions.push(function (plot, options) {
            plot.hooks.bindEvents.push(function (plot, eventHolder) {
                eventHolder.bind("contextmenu", function(e) {
                    return callContextMenu(plot, e);
                });
                eventHolder.bind("mousedown", closeContextMenu);
            });

            plot.hooks.shutdown.push(function (plot, eventHolder) {
                eventHolder.unbind("contextmenu", callContextMenu);
                eventHolder.unbind("mousedown", closeContextMenu);
            });
        });
    }

    $.plot.plugins.push({
        init: init,
        options: options,
        name: 'saveas',
        version: '1.0.0'
    });
})(jQuery);
