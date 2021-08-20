"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.execute = void 0;
const shared = require("@volar/shared");
function execute(uri, position, references, connection) {
    connection.sendNotification(shared.ShowReferencesNotification.type, { textDocument: { uri }, position, references });
}
exports.execute = execute;
//# sourceMappingURL=showReferences.js.map