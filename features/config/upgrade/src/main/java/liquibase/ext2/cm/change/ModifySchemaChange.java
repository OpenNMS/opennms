/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
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

package liquibase.ext2.cm.change;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = "modifySchema", description = "Modifies an existing schema.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ModifySchemaChange extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ModifySchemaChange.class);

    private String schemaId;
    private final List<SqlStatement> statements = new ArrayList<>();
    private final List<String> validationErrorsWhileParsing = new ArrayList<>();

    @Override
    protected ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        this.validationErrorsWhileParsing.forEach(validationErrors::addError);
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        String modifications = this.statements.stream().map(SqlStatement::toString).collect(Collectors.joining("\n"));
        return String.format("Modified schema %s:%n%s" , this.schemaId, modifications);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        LOG.info("Modifying schema {}", this.schemaId);
        return statements.toArray(new SqlStatement[0]);
    }

    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) {
        for(ParsedNode node : parsedNode.getChildren()) {
            if("put".equals(node.getName())) {
                handlePut(node);
            } else if("delete".equals(node.getName())) {
               handleDelete(node);
            }
        }
    }

    private void handlePut(ParsedNode node) {
        try {
        String name = getAttributeValueOrThrowException(node.getChildren(), "name");
        String defaultValue = getAttributeValueOrThrowException(node.getChildren(), "default");
        String regex =  getAttributeValueOrThrowException(node.getChildren(), "regex");
        String asString = String.format("PUT: name=%s, defaultValue=%s, regex=%s", name, defaultValue, regex);
        SqlStatement statement = new GenericCmStatement((ConfigurationManagerService m) -> {
            // TODO: Patrick Connect to CM
            LOG.warn("TODO: Patrick Connect to CM: " + asString);
        }, asString);
        statements.add(statement);
        } catch(IllegalArgumentException e) {
            this.validationErrorsWhileParsing.add(e.getMessage());
        }
    }

    private void handleDelete(ParsedNode node) {
        try {
            String name = getAttributeValueOrThrowException(node.getChildren(), "name");
            String asString = String.format("DELETE: name=%s", name);
            SqlStatement statement = new GenericCmStatement((ConfigurationManagerService m) -> {
                // TODO: Patrick Connect to CM
                LOG.warn("TODO: Patrick Connect to CM: " + asString);
            }, asString);
            statements.add(statement);
        } catch(IllegalArgumentException e) {
            this.validationErrorsWhileParsing.add(e.getMessage());
        }
    }

    private String getAttributeValueOrThrowException(final List<ParsedNode> listOfAttributes, final String name) {
        return listOfAttributes
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findAny()
                .map(ParsedNode::getValue)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Attribute %s must not be null.", name)));
    }
    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
}
