/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
/**
 * Extends the AsciiDoc syntax to support a tabset. The tabset is created from
 * a dlist enclosed in an example block that is marked with the tabs style.
 *
 * Usage:
 *
 *  [tabs]
 *  ====
 *  Tab A::
 *  +
 *  --
 *  Contents of tab A.
 *  --
 *  Tab B::
 *  +
 *  --
 *  Contents of tab B.
 *  --
 *  ====
 *
 * @author Dan Allen <dan@opendevise.com>
 */
const IdSeparatorCh = '-'
const ExtraIdSeparatorsRx = /^-+|-+$|-(-)+/g
const InvalidIdCharsRx = /[^a-zA-Z0-9_]/g
const List = Opal.const_get_local(Opal.module(null, 'Asciidoctor'), 'List')
const ListItem = Opal.const_get_local(Opal.module(null, 'Asciidoctor'), 'ListItem')

const generateId = (str, idx) =>
  `tabset${idx}_${str.toLowerCase().replace(InvalidIdCharsRx, IdSeparatorCh).replace(ExtraIdSeparatorsRx, '$1')}`

function tabsBlock () {
  this.onContext('example')
  this.process((parent, reader, attrs) => {
    const createHtmlFragment = (html) => this.createBlock(parent, 'pass', html)
    const tabsetIdx = parent.getDocument().counter('idx-tabset')
    const nodes = []
    nodes.push(createHtmlFragment('<div class="tabset is-loading">'))
    const container = this.parseContent(this.createBlock(parent, 'open'), reader)
    const sourceTabs = container.getBlocks()[0]
    if (!(sourceTabs && sourceTabs.getContext() === 'dlist' && sourceTabs.getItems().length)) return
    const tabs = List.$new(parent, 'ulist')
    tabs.addRole('tabs')
    const panes = {}
    sourceTabs.getItems().forEach(([[title], details]) => {
      const tab = ListItem.$new(tabs)
      tabs.$append(tab)
      const id = generateId(title.getText(), tabsetIdx)
      tab.text = `[[${id}]]${title.text}`
      let blocks = details.getBlocks()
      const numBlocks = blocks.length
      if (numBlocks) {
        if (blocks[0].context === 'open' && numBlocks === 1) blocks = blocks[0].getBlocks()
        panes[id] = blocks.map((block) => (block.parent = parent) && block)
      }
    })
    nodes.push(tabs)
    nodes.push(createHtmlFragment('<div class="content">'))
    Object.entries(panes).forEach(([id, blocks]) => {
      nodes.push(createHtmlFragment(`<div class="tab-pane" aria-labelledby="${id}">`))
      nodes.push(...blocks)
      nodes.push(createHtmlFragment('</div>'))
    })
    nodes.push(createHtmlFragment('</div>'))
    nodes.push(createHtmlFragment('</div>'))
    parent.blocks.push(...nodes)
  })
}

function register (registry) {
  registry.block('tabs', tabsBlock)
}

module.exports.register = register
