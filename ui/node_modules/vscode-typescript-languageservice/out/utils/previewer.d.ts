import type * as Proto from '../protocol';
import type { TextDocument } from 'vscode-languageserver-textdocument';
export interface IFilePathToResourceConverter {
    /**
     * Convert a typescript filepath to a VS Code resource.
     */
    toResource(filepath: string): string;
}
export declare function plainWithLinks(parts: readonly Proto.SymbolDisplayPart[] | string, filePathConverter: IFilePathToResourceConverter, getTextDocument: (uri: string) => TextDocument | undefined): string;
export declare function tagsMarkdownPreview(tags: readonly ts.JSDocTagInfo[], filePathConverter: IFilePathToResourceConverter, getTextDocument: (uri: string) => TextDocument | undefined): string;
export declare function markdownDocumentation(documentation: Proto.SymbolDisplayPart[] | string | undefined, tags: ts.JSDocTagInfo[] | undefined, filePathConverter: IFilePathToResourceConverter, getTextDocument: (uri: string) => TextDocument | undefined): string;
export declare function addMarkdownDocumentation(out: string, documentation: Proto.SymbolDisplayPart[] | string | undefined, tags: ts.JSDocTagInfo[] | undefined, converter: IFilePathToResourceConverter, getTextDocument: (uri: string) => TextDocument | undefined): string;
