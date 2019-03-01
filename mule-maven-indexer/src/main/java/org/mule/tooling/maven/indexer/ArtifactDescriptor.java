package org.mule.tooling.maven.indexer;

public interface ArtifactDescriptor {

  String getGroupId();

  String getArtifactId();

  String getClassifier();

  String getExtension();

  String getVersion();

  String getRepository();

}
