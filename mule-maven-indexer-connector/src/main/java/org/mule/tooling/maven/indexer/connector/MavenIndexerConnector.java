package org.mule.tooling.maven.indexer.connector;

import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.tooling.maven.indexer.connector.configuration.MavenIndexerConfiguration;

@Extension(name = "Mule Maven Indexer")
@Configurations({MavenIndexerConfiguration.class})
@Xml(prefix = "mmi")
public class MavenIndexerConnector {

}
