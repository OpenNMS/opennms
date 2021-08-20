import type * as vscode from 'vscode-languageserver';
export declare function transform<T extends {
    range: vscode.Range;
}>(locations: T[], getOtherRange: (range: vscode.Range) => vscode.Range | undefined): T[];
