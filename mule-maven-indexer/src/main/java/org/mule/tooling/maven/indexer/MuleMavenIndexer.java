package org.mule.tooling.maven.indexer;

import java.io.Closeable;
import java.util.List;

public interface MuleMavenIndexer extends Closeable {

  List<ArtifactDescriptor> search(String artifactId, long size);

  List<ArtifactDescriptor> resolveDependencies(String groupId, String artifactId, String version);

  void flush();

}
