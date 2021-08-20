import type * as vscode from 'vscode-languageserver';
export declare function transform(locations: vscode.SelectionRange[], getOtherRange: (range: vscode.Range) => vscode.Range | undefined): vscode.SelectionRange[];
