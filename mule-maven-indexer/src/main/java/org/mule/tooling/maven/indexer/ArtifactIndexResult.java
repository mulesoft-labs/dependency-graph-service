package org.mule.tooling.maven.indexer;

public interface ArtifactIndexResult {

  String getGroupId();

  String getArtifactId();

  String getClassifier();

  String getExtension();

  String getVersion();

  String getRepository();

}
