import type * as vscode from 'vscode-languageserver';
export declare function transform(locations: vscode.SymbolInformation[], getOtherLocation: (location: vscode.Location) => vscode.Location | undefined): vscode.SymbolInformation[];
