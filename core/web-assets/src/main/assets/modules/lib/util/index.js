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
export default class Util {
	static getBaseHref() {
		const base = document.getElementsByTagName('base')[0];
		if (base) {
			return base.href;
		}
		return '';
	}
	static setLocation(url) {
		if (window && window.location) {
			window.location.href = Util.getBaseHref() + url;
		}
	}
	static toggle(booleanValue, elementName) {
		var checkboxes = document.getElementsByName(elementName);
		for (var index in checkboxes) {
			if (checkboxes.hasOwnProperty(index)) {
				checkboxes[index].checked = booleanValue;
			}
		}
	}
}