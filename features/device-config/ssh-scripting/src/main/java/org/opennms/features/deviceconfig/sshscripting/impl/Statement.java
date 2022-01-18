/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.sshscripting.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import io.vavr.control.Either;

class Statement {

    static Either<List<String>, List<Statement>> parseScript(String script) {
        var l = Stream.of(script.split("\\n"))
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

    private final StatementType statementType;
    private final String string;

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

    private enum StatementType {

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
