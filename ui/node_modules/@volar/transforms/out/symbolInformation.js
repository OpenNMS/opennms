"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.transform = void 0;
function transform(symbol, getOtherLocation) {
    const location = getOtherLocation(symbol.location);
    if (!location)
        return;
    return {
        ...symbol,
        location,
    };
}
exports.transform = transform;
//# sourceMappingURL=symbolInformation.js.map