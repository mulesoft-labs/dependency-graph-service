package org.mule.tooling.maven.indexer;

import java.io.Closeable;
import java.util.List;

public interface MuleMavenIndexer extends Closeable {

  List<ArtifactIndexResult> search(String artifactId);

}
