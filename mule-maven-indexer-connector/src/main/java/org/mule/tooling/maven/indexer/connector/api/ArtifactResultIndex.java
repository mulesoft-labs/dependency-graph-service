package org.mule.tooling.maven.indexer.connector.api;

public class ArtifactResultIndex {

  private String groupId;
  private String artifactId;
  private String classifier;
  private String extension;
  private String version;

  private String repository;

  public ArtifactResultIndex() {
    // no op
  }

  public ArtifactResultIndex(String repository, String groupId, String artifactId, String classifier, String extension, String version) {
    this.repository = repository;

    this.groupId = groupId;
    this.artifactId = artifactId;
    this.classifier = classifier;
    this.extension = extension;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getExtension() {
    return extension;
  }

  public String getVersion() {
    return version;
  }

  public String getRepository() {
    return repository;
  }

}
