package org.mule.tooling.maven.indexer;

import static java.util.Objects.requireNonNull;

import org.mule.maven.client.api.model.RemoteRepository;
import org.mule.tooling.maven.indexer.model.DefaultMuleMavenIndexerConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public interface MuleMavenIndexerConfiguration {

  File workingDirectory();

  List<RemoteRepository> remoteRepositories();

  static MuleMavenIndexerConfigurationBuilder newMuleMavenIndexerConfigurationBuilder() {
    return new MuleMavenIndexerConfigurationBuilder();
  }

  class MuleMavenIndexerConfigurationBuilder {

    private File workingDirectory;
    private List<RemoteRepository> remoteRepositories = new LinkedList<>();

    /**
     * @param workingDirectory the working directory to store the indexes.
     * @return this
     */
    public MuleMavenIndexerConfigurationBuilder workingDirectory(File workingDirectory) {
      requireNonNull(workingDirectory, "workingDirectory cannot be null");
      this.workingDirectory = workingDirectory;
      return this;
    }

    /**
     * Adds a new remote repository to be indexed.
     *
     * @param remoteRepository a remote maven repository
     * @return this
     */
    public MuleMavenIndexerConfigurationBuilder remoteRepository(RemoteRepository remoteRepository) {
      requireNonNull(remoteRepository, "remoteRepository cannot be null");
      this.remoteRepositories.add(remoteRepository);
      return this;
    }

    /**
     * Builds the {@link MuleMavenIndexerConfiguration} object.
     *
     * @return {@link MuleMavenIndexerConfiguration} with the value sets.
     */
    public MuleMavenIndexerConfiguration build() {
      return new DefaultMuleMavenIndexerConfiguration(workingDirectory, remoteRepositories);
    }
  }
}
