"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.transform = void 0;
function transform(hover, getOtherRange) {
    if (!(hover === null || hover === void 0 ? void 0 : hover.range)) {
        return hover;
    }
    const range = getOtherRange(hover.range);
    if (!range)
        return;
    return {
        ...hover,
        range,
    };
}
exports.transform = transform;
//# sourceMappingURL=hover.js.map