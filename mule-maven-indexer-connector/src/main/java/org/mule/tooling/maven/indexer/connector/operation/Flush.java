package org.mule.tooling.maven.indexer.connector.operation;

import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.tooling.maven.indexer.connector.configuration.MavenIndexerConfiguration;

public class Flush {

  public void flush(@Config MavenIndexerConfiguration configuration) {
    configuration.getMavenIndexer().flush();
  }

}
