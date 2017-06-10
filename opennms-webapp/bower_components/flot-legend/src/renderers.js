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
