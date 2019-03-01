package org.mule.tooling.maven.indexer.connector.operation;

import static java.util.stream.Collectors.toList;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.tooling.maven.indexer.connector.api.Artifact;
import org.mule.tooling.maven.indexer.connector.configuration.MavenIndexerConfiguration;

import java.util.List;

public class ResolveDependencies {

  public List<Artifact> resolveDependencies(String groupId, String artifactId, String version,
                               @Config MavenIndexerConfiguration configuration) {
    return configuration.getMavenIndexer().resolveDependencies(groupId, artifactId, version).stream()
            .map(artifactIndexResult -> new Artifact(artifactIndexResult.getRepository(),
                                                     artifactIndexResult.getGroupId(),
                                                     artifactIndexResult.getArtifactId(),
                                                     artifactIndexResult.getClassifier(),
                                                     artifactIndexResult.getExtension(),
                                                     artifactIndexResult.getVersion()))
            .collect(toList());
  }

}
