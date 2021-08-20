"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.withRanges = exports.withCallHierarchyOutgoingCalls = exports.withCallHierarchyIncomingCalls = exports.withLocationLinks = exports.withLocations = exports.withSymbolInformations = exports.withDiagnostics = exports.withTextEdits = exports.withCodeAction = exports.createLocationSet = void 0;
function createLocationSet() {
    const set = new Set();
    return {
        add,
        has,
    };
    function add(item) {
        if (has(item)) {
            return false;
        }
        set.add(getKey(item));
        return true;
    }
    function has(item) {
        return set.has(getKey(item));
    }
    function getKey(item) {
        return [
            item.uri,
            item.range.start.line,
            item.range.start.character,
            item.range.end.line,
            item.range.end.character,
        ].join(':');
    }
}
exports.createLocationSet = createLocationSet;
function withCodeAction(items) {
    return dedupe(items, item => [
        item.title
    ].join(':'));
}
exports.withCodeAction = withCodeAction;
function withTextEdits(items) {
    return dedupe(items, item => [
        item.range.start.line,
        item.range.start.character,
        item.range.end.line,
        item.range.end.character,
        item.newText,
    ].join(':'));
}
exports.withTextEdits = withTextEdits;
function withDiagnostics(items) {
    return dedupe(items, item => [
        item.range.start.line,
        item.range.start.character,
        item.range.end.line,
        item.range.end.character,
        item.source,
        item.code,
        item.severity,
        item.message,
    ].join(':'));
}
exports.withDiagnostics = withDiagnostics;
function withSymbolInformations(items) {
    return dedupe(items, item => [
        item.name,
        item.kind,
        item.location.uri,
        item.location.range.start.line,
        item.location.range.start.character,
        item.location.range.end.line,
        item.location.range.end.character,
    ].join(':'));
}
exports.withSymbolInformations = withSymbolInformations;
function withLocations(items) {
    return dedupe(items, item => [
        item.uri,
        item.range.start.line,
        item.range.start.character,
        item.range.end.line,
        item.range.end.character,
    ].join(':'));
}
exports.withLocations = withLocations;
function withLocationLinks(items) {
    return dedupe(items, item => [
        item.targetUri,
        item.targetSelectionRange.start.line,
        item.targetSelectionRange.start.character,
        item.targetSelectionRange.end.line,
        item.targetSelectionRange.end.character,
        item.targetRange.start.line,
        item.targetRange.start.character,
        item.targetRange.end.line,
        item.targetRange.end.character,
    ].join(':'));
}
exports.withLocationLinks = withLocationLinks;
function withCallHierarchyIncomingCalls(items) {
    return dedupe(items, item => [
        item.from.uri,
        item.from.range.start.line,
        item.from.range.start.character,
        item.from.range.end.line,
        item.from.range.end.character,
    ].join(':'));
}
exports.withCallHierarchyIncomingCalls = withCallHierarchyIncomingCalls;
function withCallHierarchyOutgoingCalls(items) {
    return dedupe(items, item => [
        item.to.uri,
        item.to.range.start.line,
        item.to.range.start.character,
        item.to.range.end.line,
        item.to.range.end.character,
    ].join(':'));
}
exports.withCallHierarchyOutgoingCalls = withCallHierarchyOutgoingCalls;
function withRanges(items) {
    return dedupe(items, item => [
        item.start.line,
        item.start.character,
        item.end.line,
        item.end.character,
    ].join(':'));
}
exports.withRanges = withRanges;
function dedupe(items, getKey) {
    const map = new Map();
    for (const item of items.reverse()) {
        map.set(getKey(item), item);
    }
    return [...map.values()];
}
//# sourceMappingURL=dedupe.js.map