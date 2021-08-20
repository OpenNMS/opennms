import type * as vscode from 'vscode-languageserver';
export declare function transform(hover: vscode.Hover, getOtherRange: (range: vscode.Range) => vscode.Range | undefined): vscode.Hover | undefined;
