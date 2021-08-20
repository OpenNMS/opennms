import type { CompletionList, Range } from 'vscode-languageserver-types';
export declare function transform(completionList: CompletionList, getOtherRange: (range: Range) => Range | undefined): CompletionList;
