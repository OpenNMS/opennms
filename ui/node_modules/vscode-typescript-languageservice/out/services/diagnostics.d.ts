import * as vscode from 'vscode-languageserver';
import type * as ts from 'typescript/lib/tsserverlibrary';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined, ts: typeof import('typescript/lib/tsserverlibrary')): (uri: string, options: {
    semantic?: boolean;
    syntactic?: boolean;
    suggestion?: boolean;
    declaration?: boolean;
}, cancellationToken?: ts.CancellationToken | undefined) => vscode.Diagnostic[];
export declare function getEmitDeclarations(compilerOptions: ts.CompilerOptions): boolean;
