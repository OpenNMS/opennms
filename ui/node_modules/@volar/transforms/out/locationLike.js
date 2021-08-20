"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.transform = void 0;
function transform(location, getOtherRange) {
    const range = getOtherRange(location.range);
    if (!range)
        return;
    return {
        ...location,
        range,
    };
}
exports.transform = transform;
//# sourceMappingURL=locationLike.js.map