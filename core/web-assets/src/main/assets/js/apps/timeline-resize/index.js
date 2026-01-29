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
const debounce = require('lodash.debounce');

const DEBOUNCE_RATE = 200; // ms
const RELATIVE_SIZE = 0.5;

const getSize = function(element) {
  // if we can get the offset width of the actual <td>, use it
  const td = element.closest('td')[0];

  if (td !== undefined) {
    // get the td's padding
    const s = getComputedStyle(td).padding;
    // remove 'px' from string like '4.8px' and convert to float
    const p = parseFloat(s.substr(0, s.length - 2));
    // subtract the padding twice
    return Math.round(td.offsetWidth - 2 * p);
  }

  // otherwise, fall back to the old way of calculating
  const container = element.closest('div'); // This is the panel, not the cell that contains the IMG
  if (container !== undefined) {
    return Math.round(container.width() * RELATIVE_SIZE);
  }

  return NaN;
}

const recalculateBox = debounce(() => {
  const e = $('#availability-box');
  // Update the timeline headers
  const imgs = e.find('img');
  for (let i=0; i < imgs.length; i++) {
    const img = $(imgs[i]);
    const w = getSize(img);
    if (w) {
      const imgsrc = img.data('imgsrc') + w;
      img.attr('src', imgsrc);
    }
  }
  // Update the timeline html/images
  const spans = e.find('span');
  for (let i=0; i < spans.length; i++) {
    const span = $(spans[i]);
    const w = getSize(span);
    if (w && span.data('src')) {
      const htmlsrc = span.data('src') + w;
      span.load(String(htmlsrc));
    }
  }
}, DEBOUNCE_RATE);

$(document).ready(recalculateBox);
window.addEventListener('resize', recalculateBox);
