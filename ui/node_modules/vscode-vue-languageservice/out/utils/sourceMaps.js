"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.TeleportSourceMap = exports.PugSourceMap = exports.HtmlSourceMap = exports.JsonSourceMap = exports.CssSourceMap = exports.TsSourceMap = void 0;
const SourceMaps = require("@volar/source-map");
class TsSourceMap extends SourceMaps.SourceMap {
    constructor(sourceDocument, mappedDocument, lsType, isInterpolation, capabilities, mappings) {
        super(sourceDocument, mappedDocument, mappings);
        this.sourceDocument = sourceDocument;
        this.mappedDocument = mappedDocument;
        this.lsType = lsType;
        this.isInterpolation = isInterpolation;
        this.capabilities = capabilities;
    }
}
exports.TsSourceMap = TsSourceMap;
class CssSourceMap extends SourceMaps.SourceMap {
    constructor(sourceDocument, mappedDocument, stylesheet, module, scoped, links, capabilities, mappings) {
        super(sourceDocument, mappedDocument, mappings);
        this.sourceDocument = sourceDocument;
        this.mappedDocument = mappedDocument;
        this.stylesheet = stylesheet;
        this.module = module;
        this.scoped = scoped;
        this.links = links;
        this.capabilities = capabilities;
    }
}
exports.CssSourceMap = CssSourceMap;
class JsonSourceMap extends SourceMaps.SourceMap {
    constructor(sourceDocument, mappedDocument, jsonDocument, mappings) {
        super(sourceDocument, mappedDocument, mappings);
        this.sourceDocument = sourceDocument;
        this.mappedDocument = mappedDocument;
        this.jsonDocument = jsonDocument;
    }
}
exports.JsonSourceMap = JsonSourceMap;
class HtmlSourceMap extends SourceMaps.SourceMap {
    constructor(sourceDocument, mappedDocument, htmlDocument, language = 'html', mappings) {
        super(sourceDocument, mappedDocument, mappings);
        this.sourceDocument = sourceDocument;
        this.mappedDocument = mappedDocument;
        this.htmlDocument = htmlDocument;
        this.language = language;
    }
}
exports.HtmlSourceMap = HtmlSourceMap;
class PugSourceMap extends SourceMaps.SourceMap {
    constructor(sourceDocument, mappedDocument, pugDocument, language = 'pug') {
        super(sourceDocument, mappedDocument);
        this.sourceDocument = sourceDocument;
        this.mappedDocument = mappedDocument;
        this.pugDocument = pugDocument;
        this.language = language;
    }
}
exports.PugSourceMap = PugSourceMap;
class TeleportSourceMap extends SourceMaps.SourceMap {
    constructor(document, allowCrossFile) {
        super(document, document);
        this.document = document;
        this.allowCrossFile = allowCrossFile;
    }
    findTeleports(start, end) {
        const result = [];
        for (const teleRange of this.getMappedRanges(start, end)) {
            result.push({
                ...teleRange,
                sideData: teleRange.data.toTarget,
            });
        }
        for (const teleRange of this.getSourceRanges(start, end)) {
            result.push({
                ...teleRange,
                sideData: teleRange.data.toSource,
            });
        }
        return result;
    }
    findTeleports2(start, end) {
        const result = [];
        for (const teleRange of this.getMappedRanges2(start, end)) {
            result.push({
                ...teleRange,
                sideData: teleRange.data.toTarget,
            });
        }
        for (const teleRange of this.getSourceRanges2(start, end)) {
            result.push({
                ...teleRange,
                sideData: teleRange.data.toSource,
            });
        }
        return result;
    }
}
exports.TeleportSourceMap = TeleportSourceMap;
__exportStar(require("@volar/source-map"), exports);
//# sourceMappingURL=sourceMaps.js.map