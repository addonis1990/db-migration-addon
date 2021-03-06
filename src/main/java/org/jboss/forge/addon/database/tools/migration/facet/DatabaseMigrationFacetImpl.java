/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.database.tools.migration.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.jboss.forge.addon.database.tools.connections.ConnectionProfile;
import org.jboss.forge.addon.database.tools.migration.properties.ConnectionPropertiesBuilder;
import org.jboss.forge.addon.database.tools.migration.resource.changelog.ChangeLogResource;
import org.jboss.forge.addon.database.tools.migration.util.Constants;
import org.jboss.forge.addon.database.tools.migration.util.Utils;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.facets.constraints.FacetConstraints;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;

/**
 * @author <a href="mailto:wicem.zrelly@gmail.com">Wissem Zrelli</a>
 *
 */

@FacetConstraints({
         @FacetConstraint(DependencyFacet.class),
         @FacetConstraint(MetadataFacet.class),
         @FacetConstraint(ResourcesFacet.class)
})
public class DatabaseMigrationFacetImpl extends AbstractFacet<Project> implements DatabaseMigrationFacet
{

   private DependencyBuilder createLiquibaseDependency()
   {
      return DependencyBuilder.create()
               .setGroupId("org.liquibase")
               .setArtifactId("liquibase-maven-plugin")
               .setVersion(Constants.LIQUIBASE_DEFAULT_VERSION)
               .setPackaging("pom")
               .setScopeType("provided");
   }

   public String getLiquibaseDefaultVersion()
   {
      return Constants.LIQUIBASE_DEFAULT_VERSION;
   }

   public String getInstalledVersion()
   {
      MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);
      return metadataFacet.getDirectProperty(Constants.LIQUIBASE_VERSION_PROPERTY_NAME);
   }

   @Override
   public boolean install()
   {
      DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);

      if (!isInstalled())
      {
         createMigrationDirectory();
         dependencyFacet.addDirectManagedDependency(createLiquibaseDependency());
         metadataFacet
                  .setDirectProperty(Constants.LIQUIBASE_VERSION_PROPERTY_NAME, Constants.LIQUIBASE_DEFAULT_VERSION);
      }
      return true;
   }

   public void createMigrationDirectory()
   {
      ResourcesFacet resourcesFacet = getFaceted().getFacet(ResourcesFacet.class);
      resourcesFacet.getResourceDirectory().getOrCreateChildDirectory(Constants.DEFAULT_MIGRATION_DIRECTORY);
   }

   @Override
   public boolean isInstalled()
   {
      DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      return dependencyFacet.hasDirectManagedDependency(createLiquibaseDependency())
               && getFaceted().getFacet(ResourcesFacet.class).getResourceDirectory()
                        .getChildDirectory(Constants.DEFAULT_MIGRATION_DIRECTORY).exists();
   }

   public void setPropertiesFile(ConnectionProfile connection)
   {
      String liquibaseVersion = getFaceted().getFacet(MetadataFacet.class).getDirectProperty(
               Constants.LIQUIBASE_VERSION_PROPERTY_NAME);
      ConnectionPropertiesBuilder pBuilder = ConnectionPropertiesBuilder.create();
      pBuilder.setConnection(connection).setLiquibaseVersion(liquibaseVersion);

      ResourcesFacet projectResourcesFacet = getFaceted().getFacet(ResourcesFacet.class);
      DirectoryResource migrationDir = projectResourcesFacet.getResourceDirectory().getChildDirectory(
               Constants.DEFAULT_MIGRATION_DIRECTORY);

      FileResource<?> propertiesFile = migrationDir.getChild(Constants.PROPERTIES_FILE).reify(
               FileResource.class);
      propertiesFile.setContents(pBuilder.toString());
   }

   private DirectoryResource getMigrationDirectory(ResourcesFacet resourcesFacet)
   {
      return resourcesFacet.getResourceDirectory().getChildDirectory(
               Constants.DEFAULT_MIGRATION_DIRECTORY);
   }

   public Properties getProperties()
   {
      Properties properties = new Properties();
      DirectoryResource migrationDir = getMigrationDirectory(getFaceted().getFacet(ResourcesFacet.class));
      try
      {
         properties.load(migrationDir.getChild(Constants.PROPERTIES_FILE).getResourceInputStream());
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return properties;
   }

   @Override
   public List<String> getLiquibaseVersions()
   {
      List<String> versions = new ArrayList<String>();
      versions.add(Constants.LIQUIBASE_DEFAULT_VERSION);

      return versions;

   }

   @Override
   public List<String> getGenerationModes()
   {
      List<String> modes = new ArrayList<String>();
      modes.add(Constants.MODE_EMPTY_CHANGELOG);
      modes.add(Constants.MODE_CHANGELOG_FROM_DB);

      return modes;
   }

   @Override
   public ChangeLogResource getMasterChangeLog()
   {
      DirectoryResource migrationDir = getMigrationDirectory(getFaceted().getFacet(ResourcesFacet.class));
      return migrationDir.getChild(Constants.MASTER_CHANGELOG).reify(ChangeLogResource.class);
   }

}
