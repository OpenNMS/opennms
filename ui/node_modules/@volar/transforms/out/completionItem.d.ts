import type * as vscode from 'vscode-languageserver';
export declare function transform(item: vscode.CompletionItem, getOtherRange: (range: vscode.Range) => vscode.Range | undefined): vscode.CompletionItem;
