package org.mule.tooling.maven.indexer.connector.operation;

import static java.util.stream.Collectors.toList;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.tooling.maven.indexer.connector.api.ArtifactResultIndex;
import org.mule.tooling.maven.indexer.connector.configuration.MavenIndexerConfiguration;

import java.util.List;

public class Search {

  public List<ArtifactResultIndex> search(String artifactId, @Config MavenIndexerConfiguration configuration) {
    return configuration.getMavenIndexer().search(artifactId).stream()
            .map(artifactIndexResult -> new ArtifactResultIndex(artifactIndexResult.getRepository(),
                                                                artifactIndexResult.getGroupId(),
                                                                artifactIndexResult.getArtifactId(),
                                                                artifactIndexResult.getClassifier(),
                                                                artifactIndexResult.getExtension(),
                                                                artifactIndexResult.getVersion()))
            .collect(toList());
  }

}
