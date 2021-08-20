import type { ApiLanguageServiceContext } from '../types';
import type { CodeAction } from 'vscode-languageserver-types';
export declare function register({ sourceFiles, getTsLs }: ApiLanguageServiceContext): (codeAction: CodeAction) => Promise<CodeAction>;
