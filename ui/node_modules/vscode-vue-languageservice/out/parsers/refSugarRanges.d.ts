import type * as ts from 'typescript/lib/tsserverlibrary';
import type { TextRange } from './types';
export declare type ScriptSetupRanges = ReturnType<typeof parseRefSugarDeclarationRanges>;
export declare function parseRefSugarDeclarationRanges(ts: typeof import('typescript/lib/tsserverlibrary'), ast: ts.SourceFile, collectKeys: string[]): {
    flag: TextRange;
    leftBindings: TextRange[];
    rightFn: TextRange;
}[];
export declare function parseRefSugarCallRanges(ts: typeof import('typescript/lib/tsserverlibrary'), ast: ts.SourceFile, collectKeys: string[]): {
    fullRange: TextRange;
    argsRange: TextRange;
}[];
export declare function parseDeclarationRanges(ts: typeof import('typescript/lib/tsserverlibrary'), ast: ts.SourceFile): {
    flag: TextRange;
    leftIsIdentifier: boolean;
    leftBindings: TextRange[];
    right: TextRange;
    rightFn: TextRange | undefined;
}[];
export declare function parseDotValueRanges(ts: typeof import('typescript/lib/tsserverlibrary'), ast: ts.SourceFile): {
    range: TextRange;
    beforeDot: number;
}[];
