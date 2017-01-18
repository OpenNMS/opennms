(function ($) {
 "use strict";
var options = {
    legend: {
        statements: [],
        margin: {
            left: 5,
            right: 0,
            top: 0,
            bottom: 0
        },
        style: {
            fontSize: 10,
            minFontSize: 3,
            /*
            badgeSize: =fontSize
            badgeMarginRight: =fontSize*0.25 (no less than 1)
            */
            lineSpacing: 5,
        }
    }
};

var TOKENS = Object.freeze({
    'Badge': 'badge',
    'Text': 'text',
    'Unit': 'unit',
    'Lf': 'lf',
    'Newline': 'newline'
});
CanvasLegend.prototype = {};
CanvasLegend.prototype.constructor = CanvasLegend;
function CanvasLegend(plot, opts, tokens) {

    this.plot = plot;
    this.opts = opts;
    this.tokens = tokens;
    this.doRender = true;
    this.setFontSize(this.opts.legend.style.fontSize);
}

CanvasLegend.prototype.setDryRun = function(dryRun) {
    this.doRender = !dryRun;
};

CanvasLegend.prototype.getFontSize = function() {

    return this.badgeSize;
};

CanvasLegend.prototype.setFontSize = function(size) {

    this.fontSize = size;
    this.badgeSize = size;
    this.badgeMarginRight = Math.max(1, Math.round(size * 0.25));
    this.lineHeight = this.getLineHeight();
};

CanvasLegend.prototype.getBadgeSize = function() {

    return this.badgeSize;
};

CanvasLegend.prototype.updateMaxWidth = function() {

    this.maxWidth = Math.max(this.maxWidth, this.x);
};

CanvasLegend.prototype.getMaxWidth = function() {

    return this.maxWidth;
};

CanvasLegend.prototype.getLineHeight = function() {

    return this.opts.legend.style.lineSpacing + Math.max(this.badgeSize, this.fontSize);
};

CanvasLegend.prototype.getLegendHeight = function() {

    // Count the number of lines
    var numberOfLines = 0;
    var numberOfTokensOnNewline = 0;

    var self = this;
    $.each(self.tokens, function(idx) {
        var token = self.tokens[idx];
        if (self.tokens[idx].type === TOKENS.Newline) {
            numberOfLines++;
            numberOfTokensOnNewline = 0;
        } else {
            numberOfTokensOnNewline++;
        }
    });

    if (numberOfTokensOnNewline > 0) {
        numberOfLines++;
    }

    return numberOfLines * this.lineHeight + options.legend.margin.top + options.legend.margin.bottom;
};

CanvasLegend.prototype.beforeDraw = function() {

    this.ctx = this.plot.getCanvas().getContext('2d');

    // Outer bounds
    this.xMin = this.opts.legend.margin.left;
    this.yMin = this.ctx.canvas.clientHeight - this.getLegendHeight() + this.opts.legend.margin.top;
    //this.xMax = ctx.canvasCtx.canvas.clientWidth - this.opts.legend.margin.right;
    //this.yMax = ctx.canvasCtx.canvas.clientHeight - this.opts.legend.margin.bottom;

    // Initial coordinates
    this.x = this.xMin;
    this.y = this.yMin;

    this.maxWidth = this.x;
};

CanvasLegend.prototype.drawText = function(text) {

    this.ctx.fillStyle = "black";
    this.ctx.font = this.fontSize + "px Monospace";
    this.ctx.textAlign = "left";

    if (this.doRender) {
        this.ctx.fillText(text, this.x, this.y + this.fontSize);
    }

    var textSize = this.ctx.measureText(text);
    this.x += textSize.width;
    this.updateMaxWidth();
};

CanvasLegend.prototype.drawBadge = function(color) {

    if (this.doRender) {
        this.ctx.fillStyle = color;
        this.ctx.fillRect(this.x, this.y, this.badgeSize, this.badgeSize);

        this.ctx.beginPath();
        this.ctx.lineHeight = "0.5";
        this.ctx.strokeStyle = "black";
        this.ctx.rect(this.x, this.y, this.badgeSize, this.badgeSize);
        this.ctx.stroke();
    }

    this.x += this.badgeSize + this.badgeMarginRight;
    this.updateMaxWidth();
};

CanvasLegend.prototype.drawNewline = function() {

    this.y = this.lineHeight + this.y;
    this.x = this.xMin;
};

CanvasLegend.prototype.afterDraw = function() {
    if (this.doRender) {
        this.ctx.save();
    }
};

function tokenizeStatement(value) {

    var stack = [], tokens = [], types = {}, lfRegex = /^%(\d*)(\.(\d+))?lf/;

    var accountForTokenType = function(type) {
        if (types.hasOwnProperty(type)) {
            types[type] += 1;
        } else {
            types[type] = 1;
        }
    };

    var numTokensWithType = function(type) {
        return types.hasOwnProperty(type) ? types[type] : 0;
    };

    var pushToken = function(token) {
        if (stack.length > 0) {
            tokens.push({
                type: TOKENS.Text,
                value: stack.join('')
            });
            stack = [];
            accountForTokenType(TOKENS.Text);
        }

        if (token !== undefined) {
            tokens.push(token);
            accountForTokenType(token.type);
        }
    };

    for (var i = 0, len = value.length; i < len; i++) {

        var c = value[i];
        // Grab the next character, bounded by the size of the string
        var nextc = value[Math.min(i+1, len - 1)];
        var match;

        if (c === '%' && nextc === 'g') {

            pushToken({
                type: TOKENS.Badge
            });

            i++;
        } else if (c === '%' && nextc === 's') {

            pushToken({
                type: TOKENS.Unit
            });

            i++;
        } else if (c === '%' && nextc === '%') {

            stack.push('%');

            i++;
        } else if (c == '\\' && nextc == 'n') {

            pushToken({
                type: TOKENS.Newline
            });

            i++;
        } else if (c == '\\' && nextc == 'l') {

            pushToken({
                type: TOKENS.Newline
            });

            i++;
        } else if (c == '\\' && nextc == 's') {

            pushToken({
                type: TOKENS.Newline
            });

            i++;
        } else if ( (match = lfRegex.exec(value.slice(i))) !== null) {
            var length = NaN;
            try {
                length = parseInt(match[1]);
            } catch(err) {
                // pass
            }
            var precision = NaN;
            try {
                precision = parseInt(match[3]);
            } catch(err) {
                // pass
            }

            pushToken({
                type: TOKENS.Lf,
                length: isNaN(length) ? null : length,
                precision: isNaN(precision) ? null : precision
            });

            i += match[0].length - 1;
        } else {
            stack.push(c);
        }
    }

    // Always add a space to the end of the statement if there was a badge printed
    if (numTokensWithType(TOKENS.Badge) > 0) {
        stack.push(" ");
    }

    // Add a space after the %lf statement if there is no unit
    if (numTokensWithType(TOKENS.Lf) > 0 && numTokensWithType(TOKENS.Unit) === 0 && tokens[tokens.length - 1].type === TOKENS.Lf) {
        stack.push(" ");
    }

    // Convert any remaining characters on the stack to a text token
    pushToken();

    return tokens;
}

function reduceWithAggregate(data, aggregation) {

    var i, N = data.length, total = 0, y, yMin = NaN, yMax = NaN;

    var getYFromPoint = function(point) {
        if (point === null) {
            return NaN;
        } else if (point.length === 2) {
            return point[1];
        } else if (point.length === 3) {
            return point[1] - point[2];
        } else {
            throw "Unsupported point of length " + point.length;
        }
    };

    if (aggregation === 'MIN') {

        $.each(data, function(idx) {
            y = getYFromPoint(data[idx]);
            if (isNaN(y)) {
                return;
            }
            if (isNaN(yMin) || y < yMin) {
                yMin = y;
            }
        });
        return yMin;

    } else if (aggregation === 'MAX') {

        $.each(data, function(idx) {
            y = getYFromPoint(data[idx]);
            if (isNaN(y)) {
                return;
            }
            if (isNaN(yMax)  || y > yMax) {
                yMax = y;
            }
        });
        return yMax;

    } else if (aggregation === 'AVERAGE' || aggregation === 'AVG') {

        N = 0;

        $.each(data, function(idx) {
            y = getYFromPoint(data[idx]);
            if (isNaN(y)) {
                return;
            }
            total += y;
            N++;
        });

        return N > 0 ? total / N : NaN;

    } else if (aggregation === 'LAST') {

        for(i = N-1; i >= 0; i--) {
            y = getYFromPoint(data[i]);
            if (!isNaN(y)) {
                return y;
            }
        }

        return NaN;

    } else {
        throw "Unsupported aggregation: " + aggregation;
    }
}

function getSeriesWithMetricName(metric, allSeries, options) {

    var series;

    $.each(allSeries, function(idx) {
        if (allSeries[idx].metric === metric) {
            series = allSeries[idx];
        }
    });

    if (series === undefined && options.hiddenSeries !== undefined) {
        $.each(options.hiddenSeries, function(idx) {
            if (options.hiddenSeries[idx].metric === metric) {
                series = options.hiddenSeries[idx];
            }
        });
    }

    if (series === undefined) {
        throw "No series with metric '" + metric + "' was found.";
    } else {
        return series;
    }
}

function renderStatement(statement, series, renderer) {

    // Parse the statement into a series of tokens
    var tokens = tokenizeStatement(statement.format);
    // Used to store the unit symbol from the last LF statement, we need this in the following UNIT statement
    var lastSymbol = "";
    $.each(tokens, function(idx) {
        var token = tokens[idx];

        if (token.type === TOKENS.Text) {

            renderer.drawText(token.value);

        } else if (token.type === TOKENS.Badge) {

            renderer.drawBadge(series.color);

        } else if (token.type === TOKENS.Newline) {

            renderer.drawNewline();

        } else if (token.type === TOKENS.Unit) {

            if (lastSymbol === "") {
                lastSymbol = " ";
            }

            renderer.drawText(lastSymbol + " ");

        } else if (token.type === TOKENS.Lf) {

            var value;
            if (!statement.aggregation) {
                value = statement.value;
            } else {
                value = reduceWithAggregate(series.data, statement.aggregation);
            }
            
            var scaledValue = value;
            lastSymbol = "";

            if (!isNaN(value)) {
                var prefix = d3.formatPrefix(value, token.precision);
                lastSymbol = prefix.symbol;
                scaledValue = prefix.scale(value);
            }

            var format = "";
            if (token.length !== null) {
                format += token.length;
            }
            if (token.precision !== null) {
                format += "." + token.precision;
            }
            format += "f";

            format = d3.format(format);

            renderer.drawText(format(scaledValue));

        } else {
            throw "Unsupported token: " + JSON.stringify(token);
        }
    });
}

function init(plot) {
    plot.hooks.processOptions.push(function (plot, options) {
        // Don't do anything if there are no statements in the legend
        if (options.legend.statements.length < 1) {
            return;
        }

        // Hide the existing legend
        options.legend.show = false;

        // Tokenize all of the statements
        var tokens = [];
        $.each(options.legend.statements, function(idx) {
            var statement = options.legend.statements[idx];
            tokens.push(tokenizeStatement(statement.format));
        });
        // Flatten the array
        tokens = Array.prototype.concat.apply([], tokens);

        var rendererType = CanvasLegend;
        var renderer = new rendererType(plot, options, tokens);

        // Shift the graph up by the legend height
        options.grid.margin = {
            top: 0,
            left: 0,
            bottom: renderer.getLegendHeight(),
            right: 0
        };

        var doRender = function(data) {
            renderer.beforeDraw();
            $.each(options.legend.statements, function(idx) {
                var statement = options.legend.statements[idx];

                // Find the series with the metric name if it's defined, not all statements need an associated metric
                var series = undefined;
                if (statement.metric !== undefined) {
                    series = getSeriesWithMetricName(statement.metric, data, options);
                }

                renderStatement(statement, series, renderer);
            });
            renderer.afterDraw();
        };

        plot.hooks.draw.push(function (plot) {
            var canvasWidth = plot.getCanvas().clientWidth,
                renderedWidth = 0,
                options = plot.getOptions().legend,
                fontSize = options.style.fontSize,
                minFontSize = options.style.minFontSize,
                data = plot.getData();

            // console.log('canvas width: ' + canvasWidth);
            // console.log('renderer:',renderer);
            renderer.setDryRun(true);
            do {
                renderer.setFontSize(fontSize);
                doRender(data);
                renderedWidth = renderer.getMaxWidth();
                // console.log('rendered width at font size ' + fontSize + ': ' + renderedWidth);
                if (renderedWidth > canvasWidth) {
                    if (fontSize === minFontSize) {
                        //console.log('WARNING: minimum font size reached... giving up.');
                        break;
                    }
                    fontSize--;
                } else {
                    break;
                }
            } while (renderedWidth > canvasWidth);

            // console.log('final font size: ' + fontSize);
            // console.log('final rendered width: ' + renderedWidth);
            // console.log('rendering...');
            renderer.setDryRun(false);
            doRender(data);
            // console.log('done');
        });
    });
}


    $.plot.plugins.push({
        init: init,
        options: options,
        name: 'legend',
        version: '2.0.0'
    });
})(jQuery);
