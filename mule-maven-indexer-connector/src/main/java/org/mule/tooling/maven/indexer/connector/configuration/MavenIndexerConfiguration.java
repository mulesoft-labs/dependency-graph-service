package org.mule.tooling.maven.indexer.connector.configuration;

import static org.mule.maven.client.api.model.Authentication.newAuthenticationBuilder;
import static org.mule.tooling.maven.indexer.MuleMavenIndexerConfiguration.newMuleMavenIndexerConfigurationBuilder;
import static org.mule.tooling.maven.indexer.MuleMavenIndexerFactory.createIndexer;
import org.mule.runtime.api.i18n.I18nMessageFactory;
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
import java.net.MalformedURLException;
import java.net.URL;

@Operations({Search.class, Flush.class})
public class MavenIndexerConfiguration implements Initialisable, Disposable {

  @Parameter
  private String workingDirectory;

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
    org.mule.maven.client.api.model.RemoteRepository remoteRepository = null;
    try {
      remoteRepository = remoteRepositoryBuilder
              .id(this.remoteRepository.getId())
              .url(new URL("http://google.com"))
              .build();
    }
    catch (MalformedURLException e) {
      throw new InitialisationException(I18nMessageFactory.createStaticMessage("Internal error"), e, this);
    }
    this.mavenIndexer = createIndexer(newMuleMavenIndexerConfigurationBuilder()
            .workingDirectory(new File(workingDirectory))
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
