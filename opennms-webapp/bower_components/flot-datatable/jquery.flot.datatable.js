// Initial implementation by Andrew Hudik (2015)
(function ($) {
 "use strict";
var options = {
    datatable: {
        xaxis: {
            label: 'X',
            format: d3.format(".2f")
        },
        yaxis: {
            label: 'Y',
            ignoreColumnsWithNoLabel: false,
            format: d3.format(".2f")
        }
    }
};

function getLabelForXAxis(series, options) {
    if (series.xaxis.options.axisLabel) {
        return series.xaxis.options.axisLabel;
    }
    return options.datatable.xaxis.label;
}

function getLabelForYAxis(series, options, suffix) {
    if (series.label !== undefined && series.label !== null) {
        return series.label;
    }
    if (series.yaxis.options.axisLabel) {
        return series.yaxis.options.axisLabel;
    }
    return options.datatable.yaxis.label + suffix;
}

function createTable(allSeries, options, useRawValues) {

    var identity = function(e) { return e;},
        xformat = useRawValues ? identity : options.datatable.xaxis.format,
        yformat = useRawValues ? identity : options.datatable.yaxis.format;

    var T = '<tr><th align="left">' + getLabelForXAxis(allSeries[0], options) + '</th>',
        t = '',
        i, j, N, M, xvalue, yvalue;

    for (j = 0, N = allSeries.length; j < N; j++) {
        if (allSeries[j].nodatatable) {
            continue;
        }
        T += '<th align="left">' + getLabelForYAxis(allSeries[j], options, j) + '</th>';
    }

    T += '</tr>';
    for (N = allSeries[0].data.length, i = N-1; i >= 0; i--) {      // for each x
        xvalue = (allSeries[0].data[i] && allSeries[0].data[i].length >= 2)? allSeries[0].data[i][0] : null;
        t = '<tr><td nowrap>' + xformat(xvalue) + '</td>';    // 1st colunm, x-value
        for (j = 0, M = allSeries.length; j < M; j++) {         // for each series
            if (allSeries[j].nodatatable) {
                continue;
            }
            yvalue = (allSeries[j].data[i] && allSeries[j].data[i].length >= 2)? allSeries[j].data[i][1] : null;
            t += '<td nowrap>' + yformat(yvalue) + '</td>'; // add y-data
        }
        t += '</tr>';
        T += t;
    }

    return T;
}

function init(plot) {

    // Add the styles
    var css = document.getElementById("#jquery-flot-datatable-style");
    if (!css) {
        css = document.createElement("style");
        css.setAttribute("id", "jquery-flot-datatable-style");
        css.setAttribute("type", "text/css");
        css.innerHTML = ".flot-datatable-tab { display: inline; border: 1px solid black; border-bottom: 0; padding: 2px 5px 2px 5px; margin-left: 3px; border-radius: 4px 4px 0 0; cursor: pointer; } .flot-datatable-tab:hover { background-color: #DDDDDD; }";
        document.head.insertBefore(css, document.head.firstChild);
    }

    plot.hooks.drawOverlay.push(drawOverlay);

    function drawOverlay(plot) {
        var placeholder = plot.getPlaceholder();

        var tabsAlreadyRendered = false;
        var panel, tabs = placeholder.parent().find("#jquery-flot-datatable-tab");
        if (tabs.length > 0) {
            tabsAlreadyRendered = true;
            panel = placeholder.parent().find(".flot-datatable-data");
        } else {
            tabs = $('<div class="flot-datatable-tabs" align="right"><div class="flot-datatable-tab" id="jquery-flot-graph-tab">Graph</div><div class="flot-datatable-tab" id="jquery-flot-datatable-tab">Data</div></div>');
            panel = $('<div title="Doubleclick to copy" class="flot-datatable-data" style="width: ' + placeholder[0].clientWidth + 'px; height: ' + placeholder[0].clientHeight + 'px; padding: 0px; position: relative; overflow: scroll; background: white; z-index: 10; display: none; text-align: left;">' +
                '<input type="checkbox" name="raw" value="raw">Raw values<br>' +
                '<table style="width: 100%"></table>' +
                '</div>');
        }

        if (!tabsAlreadyRendered) {
            // Wrap the placeholder in an outer div and prepend the tabs
            placeholder.wrap("<div></div>")
                .before(tabs)
                .before(panel);

            // Copy the placeholder's style and classes to our newly created wrapper
            placeholder.parent()
                .attr('class', placeholder.attr('class'))
                .attr('style', placeholder.attr('style'))
                // Remove the height attribute
                .css("height", "");
        }

        var checkbox = panel.find(":checkbox");
        var table = panel.find("table");

        var redrawTable = function() {
            table.html(createTable(plot.getData(), plot.getOptions(), checkbox.is(':checked')));
        };
        redrawTable();

        bindTabs(tabs, panel, placeholder);
        bindCheckbox(checkbox, redrawTable);
        bindTable(table);
    };

    function bindTabs(tabs, table, placeholder) {
        tabs.click(function (e) {
            switch (e.target.id) {
                case 'jquery-flot-graph-tab':
                    table.hide();
                    placeholder.show();
                    break;
                case 'jquery-flot-datatable-tab':
                    placeholder.hide();
                    table.show();
                    break;
            }
        });
    }

    function bindCheckbox(checkbox, redrawTable) {
        checkbox.change(function() {
            redrawTable();
        });
    }

    function bindTable(table) {
        table.bind('dblclick', function () {
            highlightTableRows(table);
        });
    }

    function highlightTableRows(table) {
        var selection = window.getSelection(),
            range = document.createRange();
        range.selectNode(table.get()[0]);
        selection.removeAllRanges();
        selection.addRange(range);
    }
}


    $.plot.plugins.push({
        init: init,
        options: options,
        name: 'datatable',
        version: '1.0.6'
    });
})(jQuery);
