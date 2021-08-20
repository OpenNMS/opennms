import type * as ts from 'typescript/lib/tsserverlibrary';
import * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export declare function entriesToLocations(entries: {
    fileName: string;
    textSpan: ts.TextSpan;
}[], getTextDocument: (uri: string) => TextDocument | undefined): vscode.Location[];
export declare function entriesToLocationLinks(entries: ts.DefinitionInfo[], getTextDocument: (uri: string) => TextDocument | undefined): vscode.LocationLink[];
export declare function boundSpanToLocationLinks(info: ts.DefinitionInfoAndBoundSpan, originalDoc: TextDocument, getTextDocument: (uri: string) => TextDocument | undefined): vscode.LocationLink[];
