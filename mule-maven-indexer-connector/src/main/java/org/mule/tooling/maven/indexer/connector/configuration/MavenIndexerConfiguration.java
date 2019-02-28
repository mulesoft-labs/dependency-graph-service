package org.mule.tooling.maven.indexer.connector.configuration;

import static org.mule.maven.client.api.model.Authentication.newAuthenticationBuilder;
import static org.mule.tooling.maven.indexer.MuleMavenIndexerConfiguration.newMuleMavenIndexerConfigurationBuilder;
import static org.mule.tooling.maven.indexer.MuleMavenIndexerFactory.createIndexer;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.tooling.maven.indexer.MuleMavenIndexer;
import org.mule.tooling.maven.indexer.connector.api.RemoteRepository;
import org.mule.tooling.maven.indexer.connector.operation.Flush;
import org.mule.tooling.maven.indexer.connector.operation.Search;

import java.io.File;
import java.io.IOException;

@Operations({Search.class, Flush.class})
public class MavenIndexerConfiguration implements Initialisable, Disposable {

  @Parameter
  private File workingDirectory;

  @Parameter
  private RemoteRepository remoteRepository;

  private MuleMavenIndexer mavenIndexer;

  @Override
  public void initialise() throws InitialisationException {
    org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder remoteRepositoryBuilder = org.mule.maven.client.api.model.RemoteRepository.newRemoteRepositoryBuilder();
    if (remoteRepository.getUsername() != null && remoteRepository.getPassword() != null) {
      remoteRepositoryBuilder.authentication(newAuthenticationBuilder()
              .username(remoteRepository.getUsername())
              .password(remoteRepository.getPassword())
                                                     .build());
    }
    org.mule.maven.client.api.model.RemoteRepository remoteRepository = remoteRepositoryBuilder
            .id(this.remoteRepository.getId())
            .build();
    this.mavenIndexer = createIndexer(newMuleMavenIndexerConfigurationBuilder()
            .workingDirectory(workingDirectory)
            .remoteRepository(remoteRepository)
            .build());
  }

  @Override
  public void dispose() {
    try {
      mavenIndexer.close();
    }
    catch (IOException e) {
      //TODO how to handle with SDK?
      // no op...
    }
  }

  public MuleMavenIndexer getMavenIndexer() {
    return mavenIndexer;
  }
}
