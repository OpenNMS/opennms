"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.transform = void 0;
function transform(location, getOtherRange) {
    const range = getOtherRange(location.range);
    if (!range)
        return;
    const parent = location.parent ? transform(location.parent, getOtherRange) : undefined;
    return {
        range,
        parent,
    };
}
exports.transform = transform;
//# sourceMappingURL=selectionRange.js.map