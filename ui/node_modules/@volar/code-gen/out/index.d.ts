import type { Mapping, Mode, Range } from '@volar/source-map';
export declare type CodeGen = ReturnType<typeof createCodeGen>;
export declare function createCodeGen<T = undefined>(): {
    getText: () => string;
    getMappings: (sourceRangeParser?: ((data: T, range: Range) => Range) | undefined) => Mapping<T>[];
    addText: (str: string) => {
        start: number;
        end: number;
    };
    addCode: (str: string, sourceRange: Range, mode: Mode, data: T, extraSourceRanges?: Range[] | undefined) => {
        start: number;
        end: number;
    };
    addMapping: (str: string, sourceRange: Range, mode: Mode, data: T) => {
        start: number;
        end: number;
    };
    addMapping2: (mapping: Mapping<T>) => void;
};
export declare function margeCodeGen<T extends CodeGen>(a: T, b: T): void;
