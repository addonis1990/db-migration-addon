/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.database.tools.migration.ui;

import org.jboss.forge.addon.database.tools.migration.facet.DatabaseMigrationFacet;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import java.lang.Exception;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * @author <a href="mailto:wicem.zrelly@gmail.com">Wissem Zrelli</a>
 *
 */

public class AddLiquibaseCommand extends AbstractProjectCommand implements UICommand
{

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private DatabaseMigrationFacet databaseMigrationFacet;

   @Inject
   @WithAttributes(shortName = 'v', label = "Liquibase Version", type = InputType.DROPDOWN)
   private UISelectOne<String> liquibaseVersion;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(AddLiquibaseCommand.class)
               .name("DBMA: Add")
               .description("This will add the Liquibase dependency and create the migration directory"
                        + "where we will store all DBMA Files");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(liquibaseVersion);
      liquibaseVersion.setDefaultValue("");
      liquibaseVersion.setValueChoices(new Callable<Iterable<String>>()
      {
         @Override
         public Iterable<String> call() throws Exception
         {
            return databaseMigrationFacet.getLiquibaseVersions();
         }
      });
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      if (liquibaseVersion.getValue().equals(""))
         validator.addValidationError(liquibaseVersion,
                  "Please select a Liquibase version");
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      facetFactory.install(getSelectedProject(context), databaseMigrationFacet);
      return Results.success("Created migration directory and added liquibase dependency");
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      Boolean parent = super.isEnabled(context);
      if (parent)
      {
         return !getSelectedProject(context).hasFacet(DatabaseMigrationFacet.class);
      }
      return parent;
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Inject
   private ProjectFactory projectFactory;

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }
}