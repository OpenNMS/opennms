"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.htmlToPug = exports.pugToHtml = void 0;
const pug = require("pug");
const htmlparser2 = require("htmlparser2");
const domhandler_1 = require("domhandler");
const domelementtype_1 = require("domelementtype");
const tabSize = 2;
const useTabs = false;
function pugToHtml(pugCode) {
    pugCode = pugCode.replace(/\/\/-/g, '// ');
    let htmlCode = pug.compile(pugCode, { doctype: 'html' })();
    htmlCode = htmlCode.replace(/\-\-\>/g, " -->");
    // TODO: use [Unescaped Attributes](https://pugjs.org/language/attributes.html#unescaped-attributes)
    htmlCode = htmlCode
        .replace(/&gt;/g, `>`)
        .replace(/&lt;/g, `<`)
        .replace(/&amp;/g, `&`)
        .replace(/&quot;/g, `"`);
    return htmlCode.trim();
}
exports.pugToHtml = pugToHtml;
function htmlToPug(html) {
    let nodes = htmlparser2.parseDOM(html, {
        xmlMode: false,
        lowerCaseTags: false,
        lowerCaseAttributeNames: false,
        recognizeSelfClosing: true,
    });
    nodes = filterEmptyTextNodes(nodes);
    let pug = '';
    for (const node of nodes) {
        worker(node, false);
    }
    return pug;
    function createIndent(useTabs, tabSize, indent) {
        return useTabs ? '\t'.repeat(indent) : ' '.repeat(indent * tabSize);
    }
    function getIndent(indent) {
        return createIndent(useTabs, tabSize, indent);
    }
    function worker(node, inlineChild, indent = 0) {
        if (node.type === domelementtype_1.ElementType.Text) {
            const dataNode = node;
            // single line
            if (dataNode.data.indexOf('\n') === -1) {
                if (inlineChild) {
                    pug += ' ' + dataNode.data;
                }
                else {
                    pug += '\n' + getIndent(indent) + '| ' + dataNode.data;
                }
            }
            // mutli-line
            else {
                pug += '\n' + getIndent(indent) + '.';
                for (const line of dataNode.data.split('\n')) {
                    pug += '\n' + getIndent(indent + 1) + line.trim();
                }
            }
        }
        else if (node.type === domelementtype_1.ElementType.Tag) {
            const element = node;
            let code = element.name !== 'div' ? element.name : '';
            const atts = [];
            for (const att in element.attribs) {
                if (att === 'id') {
                    const val = element.attribs[att];
                    code += `#${val}`;
                }
            }
            for (const att in element.attribs) {
                if (att === 'class') {
                    const val = element.attribs[att];
                    if (!val.match(/[^\w-_ ]/)) {
                        const classes = val.split(' ');
                        for (const _class of classes) {
                            if (!_class.trim())
                                continue;
                            code += `.${_class}`;
                        }
                    }
                    else {
                        atts.push(`${att}="${val}"`);
                    }
                }
            }
            for (const att in element.attribs) {
                if (att !== 'id' && att !== 'class') {
                    let val = element.attribs[att];
                    if (val === '') {
                        atts.push(`${att}`);
                    }
                    else if (val.indexOf('\n') === -1) {
                        atts.push(`${att}="${val}"`);
                    }
                    else {
                        val = val.replace(/\`/g, '\\`');
                        atts.push(`${att}=\`${val}\``);
                    }
                }
            }
            if (!code) {
                code = 'div';
            }
            if (atts.length > 0) {
                code += `(${atts.join(' ')})`;
            }
            pug += '\n' + getIndent(indent) + code;
            const childs = filterEmptyTextNodes(element.children);
            for (let i = 0; i < childs.length; i++) {
                const childNode = childs[i];
                const inlineChild = i === 0 && atts.length === 0;
                worker(childNode, inlineChild, indent + 1);
            }
        }
        else if (node.type === domelementtype_1.ElementType.Comment) {
            const dataNode = node;
            const lines = dataNode.data.split('\n')
                .map(s => s.trim());
            // multi-line
            if (lines.length >= 2) {
                pug += '\n' + getIndent(indent) + '//';
                for (const line of lines) {
                    pug += '\n' + getIndent(indent + 1) + line;
                }
            }
            // single line
            else {
                for (const line of lines) {
                    pug += '\n' + getIndent(indent) + '// ' + line;
                }
            }
        }
        return pug;
    }
    function filterEmptyTextNodes(nodes) {
        const newNodes = [];
        for (const node of nodes) {
            if (node.type === domelementtype_1.ElementType.Text) {
                const dataNode = node;
                const text = dataNode.data.trim();
                if (text === '')
                    continue;
                newNodes.push(new domhandler_1.DataNode(domelementtype_1.ElementType.Text, text));
            }
            else {
                newNodes.push(node);
            }
        }
        return newNodes;
    }
}
exports.htmlToPug = htmlToPug;
//# sourceMappingURL=index.js.map