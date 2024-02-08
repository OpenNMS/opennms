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
package org.opennms.features.deviceconfig.sshscripting.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import io.vavr.control.Either;

class Statement {

    static Either<List<String>, List<Statement>> parseScript(String script) {
        var l = Stream.of(script.split("\\\\n|\\n"))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .map(line -> Pair.of(line, parseStatement(line)))
                .collect(Collectors.toList());

        var errs = l.stream().filter(pair -> pair.getRight() == null).map(Pair::getLeft).collect(Collectors.toList());
        if (!errs.isEmpty()) {
            return Either.left(errs);
        } else {
            return Either.right(l.stream().map(Pair::getRight).collect(Collectors.toList()));
        }
    }

    static Statement parseStatement(String line) {
        for (var p: StatementType.values()) {
            var s = p.parse(line);
            if (s != null) return s;
        }
        return null;
    }

    //
    // Statement implementations
    //

    final StatementType statementType;
    final String string;

    Statement(StatementType statementType, String string) {
        this.statementType = statementType;
        this.string = string;
    }

    void execute(SshInteraction interaction) throws Exception {
        var s = interaction.replaceVars(string);
        statementType.execute(interaction, s);
    }

    @Override
    public String toString() {
        return statementType.name() + ": " + string;
    }

    enum StatementType {

        send {
            @Override
            public void execute(SshInteraction interaction, String string) throws Exception {
                interaction.sendLine(string);
            }
        }, await {
            @Override
            public void execute(SshInteraction interaction, String string) throws Exception {
                interaction.await(string);
            }
        };

        public Statement parse(String line) {
            if (line.startsWith(name() + ":")) {
                var string = line.substring(name().length() + 1).trim();
                if (StringUtils.isNoneBlank(string)) {
                    return new Statement(this, string);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        public abstract void execute(SshInteraction interaction, String string) throws Exception;

    }

}
