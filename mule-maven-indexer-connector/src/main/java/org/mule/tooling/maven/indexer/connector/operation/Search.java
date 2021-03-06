package org.mule.tooling.maven.indexer.connector.operation;

import static java.util.stream.Collectors.toList;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.tooling.maven.indexer.connector.api.Artifact;
import org.mule.tooling.maven.indexer.connector.configuration.MavenIndexerConfiguration;

import java.util.List;

public class Search {

  public List<Artifact> search(String artifactId,
                               long size, @Config MavenIndexerConfiguration configuration) {
    return configuration.getMavenIndexer().search(artifactId, size).stream()
            .map(artifactIndexResult -> new Artifact(artifactIndexResult.getRepository(),
                                                     artifactIndexResult.getGroupId(),
                                                     artifactIndexResult.getArtifactId(),
                                                     artifactIndexResult.getClassifier(),
                                                     artifactIndexResult.getExtension(),
                                                     artifactIndexResult.getVersion()))
            .collect(toList());
  }

}
