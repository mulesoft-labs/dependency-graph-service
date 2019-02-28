package org.mule.tooling.maven.indexer.model;

import static java.util.Objects.requireNonNull;
import org.mule.maven.client.api.model.RemoteRepository;
import org.mule.tooling.maven.indexer.MuleMavenIndexerConfiguration;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class DefaultMuleMavenIndexerConfiguration implements MuleMavenIndexerConfiguration, Serializable {

  private File workingDirectory;
  private List<RemoteRepository> remoteRepositories;

  public DefaultMuleMavenIndexerConfiguration(File workingDirectory, List<RemoteRepository> remoteRepositories) {
    requireNonNull(workingDirectory, "workingDirectory cannot be null");

    this.workingDirectory = workingDirectory;
    this.remoteRepositories = remoteRepositories;
  }

  @Override
  public File workingDirectory() {
    return workingDirectory;
  }

  @Override
  public List<RemoteRepository> remoteRepositories() {
    return remoteRepositories;
  }

}
