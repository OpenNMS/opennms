"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.margeCodeGen = exports.createCodeGen = void 0;
function createCodeGen() {
    let text = '';
    const mappings = [];
    return {
        getText: () => text,
        getMappings,
        addText,
        addCode,
        addMapping,
        addMapping2,
    };
    function getMappings(sourceRangeParser) {
        if (!sourceRangeParser) {
            return mappings;
        }
        return mappings.map(mapping => ({
            ...mapping,
            sourceRange: sourceRangeParser(mapping.data, mapping.sourceRange),
            additional: mapping.additional
                ? mapping.additional.map(extraMapping => ({
                    ...extraMapping,
                    sourceRange: sourceRangeParser(mapping.data, extraMapping.sourceRange),
                }))
                : undefined,
        }));
    }
    function addCode(str, sourceRange, mode, data, extraSourceRanges) {
        const targetRange = addText(str);
        addMapping2({
            mappedRange: targetRange,
            sourceRange,
            mode,
            data,
            additional: extraSourceRanges ? extraSourceRanges.map(extraSourceRange => ({
                mappedRange: targetRange,
                mode,
                sourceRange: extraSourceRange,
            })) : undefined,
        });
        return targetRange;
    }
    function addMapping(str, sourceRange, mode, data) {
        const targetRange = {
            start: text.length,
            end: text.length + str.length,
        };
        addMapping2({ mappedRange: targetRange, sourceRange, mode, data });
        return targetRange;
    }
    function addMapping2(mapping) {
        mappings.push(mapping);
    }
    function addText(str) {
        const range = {
            start: text.length,
            end: text.length + str.length,
        };
        text += str;
        return range;
    }
}
exports.createCodeGen = createCodeGen;
function margeCodeGen(a, b) {
    const aLength = a.getText().length;
    for (const mapping of b.getMappings()) {
        a.addMapping2({
            ...mapping,
            mappedRange: {
                start: mapping.mappedRange.start + aLength,
                end: mapping.mappedRange.end + aLength,
            },
            additional: mapping.additional ? mapping.additional.map(mapping_2 => ({
                ...mapping_2,
                mappedRange: {
                    start: mapping_2.mappedRange.start + aLength,
                    end: mapping_2.mappedRange.end + aLength,
                },
            })) : undefined,
        });
    }
    a.addText(b.getText());
}
exports.margeCodeGen = margeCodeGen;
//# sourceMappingURL=index.js.map