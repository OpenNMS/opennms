import type * as vscode from 'vscode-languageserver';
export declare function createLocationSet(): {
    add: (item: vscode.Location) => boolean;
    has: (item: vscode.Location) => boolean;
};
export declare function withCodeAction<T extends vscode.CodeAction>(items: T[]): T[];
export declare function withTextEdits<T extends vscode.TextEdit>(items: T[]): T[];
export declare function withDiagnostics<T extends vscode.Diagnostic>(items: T[]): T[];
export declare function withSymbolInformations<T extends vscode.SymbolInformation>(items: T[]): T[];
export declare function withLocations<T extends vscode.Location>(items: T[]): T[];
export declare function withLocationLinks<T extends vscode.LocationLink>(items: T[]): T[];
export declare function withCallHierarchyIncomingCalls<T extends vscode.CallHierarchyIncomingCall>(items: T[]): T[];
export declare function withCallHierarchyOutgoingCalls<T extends vscode.CallHierarchyOutgoingCall>(items: T[]): T[];
export declare function withRanges<T extends vscode.Range>(items: T[]): T[];
