/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

const debounce = require('lodash').debounce;

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
