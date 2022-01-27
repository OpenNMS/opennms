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

import static liquibase.ext2.cm.change.Liqui2ConfigItemUtil.createConfigItemForProperty;
import static liquibase.ext2.cm.change.Liqui2ConfigItemUtil.getAttributeValueOrThrowException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
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

@DatabaseChange(name = "changeSchema", description = "Changes an existing schema.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ChangeSchema extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeSchema.class);

    private String schemaId;
    private final List<String> validationErrorsWhileParsing = new ArrayList<>();

    private List<Consumer<OpenAPIBuilder>> changes = new ArrayList<>();
    private List<String> changeLog = new ArrayList<>();

    @Override
    protected ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        this.validationErrorsWhileParsing.forEach(validationErrors::addError);
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        String modifications = String.join("\n", this.changeLog);
        return String.format("Changed schema %s:%n%s", this.schemaId, modifications);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        LOG.info("Change schema {}", this.schemaId);

        return new SqlStatement[]{
                // we wrap all changes into one statement to be atomic and save some time
                new GenericCmStatement((ConfigurationManagerService cm) -> {
                    // 1.) find schema
                    Optional<ConfigDefinition> definitionOpt = null;
                    definitionOpt = ((CmDatabase) database).getConfigurationManager().getRegisteredConfigDefinition(this.schemaId);
                    ConfigDefinition definition;
                    definition = definitionOpt.orElseGet(()->{
                        // Create a new one
                        ConfigDefinition newDefinition = new ConfigDefinition(this.schemaId, false);
                        newDefinition.setConfigName(this.schemaId);
                        cm.registerConfigDefinition(this.schemaId, newDefinition);
                        return newDefinition;
                    });

                    OpenAPIBuilder builder = OpenAPIBuilder.createBuilder(this.schemaId, this.schemaId, ConfigurationManagerService.BASE_PATH, definition.getSchema());

                    // 2.) apply all changes
                    for (Consumer<OpenAPIBuilder> change : changes) {
                        change.accept(builder);
                    }

                    // 3.) write definition
                    definition.setSchema(builder.build(false));
                    cm.changeConfigDefinition(this.schemaId, definition);
                })
        };
    }

    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) {
        for (ParsedNode node : parsedNode.getChildren()) {
            if ("put".equals(node.getName())) {
                handlePut(node);
            } else if ("delete".equals(node.getName())) {
                handleDelete(node);
            }
        }
    }

    private void handlePut(ParsedNode node) {
        try {
            final ConfigItem newPropertyDefinition = createConfigItemForProperty(node.getChildren());
            Consumer<OpenAPIBuilder> change = schema -> put(schema, newPropertyDefinition);
            changes.add(change);
        } catch (IllegalArgumentException e) {
            this.validationErrorsWhileParsing.add(e.getMessage());
        }
    }

    /**
     * remove old definition (if present) & add new one.
     */
    static void put(OpenAPIBuilder schema, ConfigItem newPropertyDefinition) {
        schema.removeAttribute(newPropertyDefinition.getName());
        schema.addAttribute(newPropertyDefinition);
    }

    private void handleDelete(ParsedNode node) {
        try {
            String name = getAttributeValueOrThrowException(node.getChildren(), "name");
            changeLog.add(String.format("DELETE: name=%s", name));
            Consumer<OpenAPIBuilder> change = openApi -> openApi.removeAttribute(name);
            changes.add(change);
        } catch (IllegalArgumentException e) {
            this.validationErrorsWhileParsing.add(e.getMessage());
        }
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
}
