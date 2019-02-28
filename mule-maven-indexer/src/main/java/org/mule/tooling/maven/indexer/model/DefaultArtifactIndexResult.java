package org.mule.tooling.maven.indexer.model;

import org.mule.tooling.maven.indexer.ArtifactIndexResult;

import java.io.Serializable;

public class DefaultArtifactIndexResult implements ArtifactIndexResult, Serializable {

  private String groupId;
  private String artifactId;
  private String classifier;
  private String extension;
  private String version;

  private String repository;

  public DefaultArtifactIndexResult() {
    // no op
  }

  public DefaultArtifactIndexResult(String repository, String groupId, String artifactId, String classifier, String extension, String version) {
    this.repository = repository;

    this.groupId = groupId;
    this.artifactId = artifactId;
    this.classifier = classifier;
    this.extension = extension;
    this.version = version;
  }

  @Override
  public String getGroupId() {
    return groupId;
  }

  @Override
  public String getArtifactId() {
    return artifactId;
  }

  @Override
  public String getClassifier() {
    return classifier;
  }

  @Override
  public String getExtension() {
    return extension;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getRepository() {
    return repository;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DefaultArtifactIndexResult that = (DefaultArtifactIndexResult) o;

    if (!groupId.equals(that.groupId)) {
      return false;
    }
    if (!artifactId.equals(that.artifactId)) {
      return false;
    }
    if (classifier != null ? !classifier.equals(that.classifier) : that.classifier != null) {
      return false;
    }
    if (extension != null ? !extension.equals(that.extension) : that.extension != null) {
      return false;
    }
    return version.equals(that.version);
  }

  @Override
  public int hashCode() {
    int result = groupId.hashCode();
    result = 31 * result + artifactId.hashCode();
    result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
    result = 31 * result + (extension != null ? extension.hashCode() : 0);
    result = 31 * result + version.hashCode();
    return result;
  }
}
