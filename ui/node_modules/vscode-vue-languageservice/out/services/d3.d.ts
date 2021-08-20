import type { ApiLanguageServiceContext } from '../types';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export declare function register({ modules: { typescript: ts }, sourceFiles, templateTsLs }: ApiLanguageServiceContext): (document: TextDocument) => Promise<string>;
