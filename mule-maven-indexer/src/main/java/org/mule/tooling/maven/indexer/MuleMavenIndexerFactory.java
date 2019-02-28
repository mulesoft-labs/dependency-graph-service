package org.mule.tooling.maven.indexer;

public class MuleMavenIndexerFactory {

  public static MuleMavenIndexer createIndexer(MuleMavenIndexerConfiguration configuration) {
    return new DefaultMuleMavenIndexer(configuration);
  }

}