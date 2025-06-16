#!/usr/bin/env node
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

const child = require('child_process');

const git = child.execSync('git --version', {
  stdio: ['pipe'],
});

if (git.toString().match(/git version 1\./)) {
  console.log(`Please upgrade your git client to version 2.x or higher: ${git}`);
  console.log('For details, see: https://npm.community/t/npm-install-ignores-version-for-git-ssh-dependency-and-gets-master-instead-when-git-client-is-1-x-x/4473');
  console.log('');
  process.exit(1);
} else {
  console.log(`git good: ${git}`);
}
