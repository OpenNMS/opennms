"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.boundSpanToLocationLinks = exports.entriesToLocationLinks = exports.entriesToLocations = void 0;
const vscode = require("vscode-languageserver");
const shared = require("@volar/shared");
function entriesToLocations(entries, getTextDocument) {
    const locations = [];
    for (const entry of entries) {
        const entryUri = shared.fsPathToUri(entry.fileName);
        const doc = getTextDocument(entryUri);
        if (!doc)
            continue;
        const range = vscode.Range.create(doc.positionAt(entry.textSpan.start), doc.positionAt(entry.textSpan.start + entry.textSpan.length));
        const uri = shared.fsPathToUri(entry.fileName);
        const location = vscode.Location.create(uri, range);
        locations.push(location);
    }
    return locations;
}
exports.entriesToLocations = entriesToLocations;
function entriesToLocationLinks(entries, getTextDocument) {
    const locations = [];
    for (const entry of entries) {
        const entryUri = shared.fsPathToUri(entry.fileName);
        const doc = getTextDocument(entryUri);
        if (!doc)
            continue;
        const targetSelectionRange = vscode.Range.create(doc.positionAt(entry.textSpan.start), doc.positionAt(entry.textSpan.start + entry.textSpan.length));
        const targetRange = entry.contextSpan ? vscode.Range.create(doc.positionAt(entry.contextSpan.start), doc.positionAt(entry.contextSpan.start + entry.contextSpan.length)) : targetSelectionRange;
        const originSelectionRange = entry.originalTextSpan ? vscode.Range.create(doc.positionAt(entry.originalTextSpan.start), doc.positionAt(entry.originalTextSpan.start + entry.originalTextSpan.length)) : undefined;
        const uri = shared.fsPathToUri(entry.fileName);
        const location = vscode.LocationLink.create(uri, targetRange, targetSelectionRange, originSelectionRange);
        locations.push(location);
    }
    return locations;
}
exports.entriesToLocationLinks = entriesToLocationLinks;
function boundSpanToLocationLinks(info, originalDoc, getTextDocument) {
    const locations = [];
    if (!info.definitions)
        return locations;
    const originSelectionRange = vscode.Range.create(originalDoc.positionAt(info.textSpan.start), originalDoc.positionAt(info.textSpan.start + info.textSpan.length));
    for (const entry of info.definitions) {
        const entryUri = shared.fsPathToUri(entry.fileName);
        const doc = getTextDocument(entryUri);
        if (!doc)
            continue;
        const targetSelectionRange = vscode.Range.create(doc.positionAt(entry.textSpan.start), doc.positionAt(entry.textSpan.start + entry.textSpan.length));
        const targetRange = entry.contextSpan ? vscode.Range.create(doc.positionAt(entry.contextSpan.start), doc.positionAt(entry.contextSpan.start + entry.contextSpan.length)) : targetSelectionRange;
        const uri = shared.fsPathToUri(entry.fileName);
        const location = vscode.LocationLink.create(uri, targetRange, targetSelectionRange, originSelectionRange);
        locations.push(location);
    }
    return locations;
}
exports.boundSpanToLocationLinks = boundSpanToLocationLinks;
//# sourceMappingURL=transforms.js.map