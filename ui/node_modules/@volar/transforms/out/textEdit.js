"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.transform = void 0;
const vscode = require("vscode-languageserver");
function transform(textEdit, getOtherRange) {
    if (vscode.TextEdit.is(textEdit)) {
        const range = getOtherRange(textEdit.range);
        if (!range)
            return;
        return {
            ...textEdit,
            range,
        };
    }
    else if (vscode.InsertReplaceEdit.is(textEdit)) {
        const insert = getOtherRange(textEdit.insert);
        if (!insert)
            return;
        const replace = getOtherRange(textEdit.replace);
        if (!replace)
            return;
        return {
            ...textEdit,
            insert,
            replace,
        };
    }
}
exports.transform = transform;
//# sourceMappingURL=textEdit.js.map