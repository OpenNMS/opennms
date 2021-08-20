import type { Range } from 'vscode-languageserver-types';
export declare function transform<T extends {
    range: Range;
}>(location: T, getOtherRange: (range: Range) => Range | undefined): T | undefined;
