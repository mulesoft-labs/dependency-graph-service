package org.mule.tooling.maven.indexer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.MavenClientProvider;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.api.model.RemoteRepository;
import org.mule.tooling.maven.indexer.model.DefaultArtifactDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IteratorResultSet;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.expr.UserInputSearchExpression;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.model.Model;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class DefaultMuleMavenIndexer implements MuleMavenIndexer {

    private final PlexusContainer plexusContainer;
    private final Indexer indexer;
    private final IndexUpdater indexUpdater;
    private final Wagon httpWagon;

    private MuleMavenIndexerConfiguration configuration;

    private Map<String, IndexResource> indexResourcesByServerId = new HashMap<>();
    private Map<String, IndexingContext> indexingContextsByServerId = new HashMap<>();

    private MavenClient mavenClient;

    public DefaultMuleMavenIndexer(MuleMavenIndexerConfiguration configuration) {
        try {
            final DefaultContainerConfiguration config = new DefaultContainerConfiguration();
            config.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
            this.plexusContainer = new DefaultPlexusContainer(config);

            // lookup the indexer components from plexus
            this.indexer = plexusContainer.lookup(Indexer.class);
            this.indexUpdater = plexusContainer.lookup(IndexUpdater.class);
            // lookup wagon used to remotely fetch index
            this.httpWagon = plexusContainer.lookup(Wagon.class, "http");

            this.configuration = configuration;

            init(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init(MuleMavenIndexerConfiguration configuration) throws IOException, ComponentLookupException {
        // Creators we want to use (search for fields it defines)
        List<IndexCreator> indexers = new ArrayList<>();
        indexers.add( plexusContainer.lookup(IndexCreator.class, "min" ) );
        indexers.add( plexusContainer.lookup(IndexCreator.class, "jarContent" ) );
        indexers.add( plexusContainer.lookup(IndexCreator.class, "maven-plugin" ) );

        if (!configuration.workingDirectory().exists()) {
            if (!configuration.workingDirectory().mkdirs()) {
                throw new IOException(format("Couldn't create the index working directory at: %s", configuration.workingDirectory()));
            }
        }

        for (RemoteRepository remoteRepository : configuration.remoteRepositories()) {
            String serverId = remoteRepository.getId();
            // Files where local cache is (if any) and Lucene Index should be located
            IndexResource indexResource = new IndexResource(serverId,
                                                    new File(configuration.workingDirectory(), serverId + "-cache"),
                                                    new File(configuration.workingDirectory(), serverId + "-index"));
            indexResourcesByServerId.put(serverId, indexResource);

            // Create context for this repository index
            IndexingContext indexingContext = indexer.createIndexingContext(serverId, serverId, indexResource.getLocalCache(), indexResource.getLocalIndex(),
                                                                            remoteRepository.getUrl().toString(), null, true, true, indexers);
            indexingContextsByServerId.put(serverId, indexingContext);

            // Update the index (incremental update will happen if this is not 1st run and files are not deleted)
            // This whole block below should not be executed on every app start, but rather controlled by some configuration
            // since this block will always emit at least one HTTP GET. Central indexes are updated once a week, but
            // other index sources might have different index publishing frequency.
            // Preferred frequency is once a week.
            if ( true )
            {
                System.out.println( "Updating Index..." );
                System.out.println( "This might take a while on first run, so please be patient!" );
                // Create ResourceFetcher implementation to be used with IndexUpdateRequest
                // Here, we use Wagon based one as shorthand, but all we need is a ResourceFetcher implementation
                TransferListener listener = new AbstractTransferListener()
                {
                    public void transferStarted( TransferEvent transferEvent )
                    {
                        System.out.print( "  Downloading " + transferEvent.getResource().getName() );
                    }

                    public void transferProgress( TransferEvent transferEvent, byte[] buffer, int length )
                    {
                    }

                    public void transferCompleted( TransferEvent transferEvent )
                    {
                        System.out.println( " - Done" );
                    }
                };

                AuthenticationInfo authenticationInfo = null;
                if (remoteRepository.getAuthentication().isPresent()) {
                    authenticationInfo = new AuthenticationInfo();
                    authenticationInfo.setUserName(remoteRepository.getAuthentication().get().getUsername());
                    authenticationInfo.setPassword(remoteRepository.getAuthentication().get().getPassword());
                }

                ResourceFetcher resourceFetcher = new WagonHelper.WagonFetcher(httpWagon, listener, authenticationInfo, null );

                Date centralContextCurrentTimestamp = indexingContext.getTimestamp();
                IndexUpdateRequest updateRequest = new IndexUpdateRequest(indexingContext, resourceFetcher );
                IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest );
                if ( updateResult.isFullUpdate() )
                {
                    System.out.println( "Full update happened!" );
                }
                else if ( updateResult.getTimestamp().equals( centralContextCurrentTimestamp ) )
                {
                    System.out.println( "No update needed, index is up to date!" );
                }
                else
                {
                    System.out.println(
                            "Incremental update happened, change covered " + centralContextCurrentTimestamp + " - "
                            + updateResult.getTimestamp() + " period." );
                }

                System.out.println();
            }

            System.out.println();
            System.out.println( "Using index" );
            System.out.println( "===========" );
            System.out.println();

        }


        MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder = MavenConfiguration.newMavenConfigurationBuilder();
        configuration.remoteRepositories().forEach(remoteRepository -> mavenConfigurationBuilder.remoteRepository(remoteRepository));
        this.mavenClient = MavenClientProvider.discoverProvider(this.getClass().getClassLoader()).createMavenClient(mavenConfigurationBuilder
                .localMavenRepositoryLocation(new File(configuration.workingDirectory(), "local"))
                                                                                                                            .build());
    }

    @Override
    public void flush() {
        try {
            FileUtils.cleanDirectory(configuration.workingDirectory());
        }
        catch (IOException e) {
            throw new UncheckedIOException("Error while flushing indexes and local repository", e);
        }
    }

    @Override
    public List<ArtifactDescriptor>  search(String artifactId, long size) {
        final Query artifactIdQ =
                indexer.constructQuery(MAVEN.ARTIFACT_ID, new UserInputSearchExpression(artifactId));
        final BooleanQuery query = new BooleanQuery();
        query.add(artifactIdQ, Occur.MUST);

        // we want "jar" artifacts only
        query.add(indexer.constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), Occur.MUST);
        // we want main artifacts only (no classifier)
        // Note: this below is unfinished API, needs fixing
        query.add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)),
                  Occur.MUST_NOT);

        Set<ArtifactDescriptor> results = new HashSet<>();
        for (IndexingContext indexingContext : indexingContextsByServerId.values()) {
            try {
                System.out.println(format("Searching for all GAVs with A=%s on index: %s", artifactId, indexingContext.getId()));
                final IteratorSearchRequest request =
                        new IteratorSearchRequest(query, Collections.singletonList(indexingContext));
                final IteratorSearchResponse response = indexer.searchIterator(request);
                transformAndAppend(results, response.iterator(), size);
                response.close();
            } catch (Exception e) {
                throw new RuntimeException("Error while searching data in index", e);
            }
        }

        return results.stream().collect(toList());
    }

    private void transformAndAppend(Set<ArtifactDescriptor> results, IteratorResultSet iterator, long size) {
        while (iterator.hasNext()) {
            ArtifactInfo artifactInfo = iterator.next();
            results.add(new DefaultArtifactDescriptor(artifactInfo.getRepository(),
                                                      artifactInfo.getGroupId(),
                                                      artifactInfo.getArtifactId(),
                                                      artifactInfo.getClassifier(),
                                                      artifactInfo.getFileExtension(),
                                                      artifactInfo.getVersion()));
            if (results.size() >= size) {
                break;
            }
        }
    }

    @Override
    public List<ArtifactDescriptor> resolveDependencies(String groupId, String artifactId, String version) {
        Model effectiveModel = mavenClient.getEffectiveModel(new File(mavenClient.resolveBundleDescriptor(new BundleDescriptor.Builder()
                                                                                                       .setGroupId(groupId).setArtifactId(artifactId).setVersion(version).setType("pom")
                                                                                                       .build()).getBundleUri()), Optional.empty());
        return effectiveModel.getDependencies().stream()
                .map(dependency -> new DefaultArtifactDescriptor(null,
                                                                 dependency.getGroupId(),
                                                                 dependency.getArtifactId(),
                                                                 dependency.getClassifier(),
                                                                 dependency.getType(),
                                                                 dependency.getVersion()))
                .collect(toList());
    }

    @Override
    public void close() throws IOException {
        for (IndexingContext indexingContext : indexingContextsByServerId.values()) {
            // close cleanly
            indexer.closeIndexingContext(indexingContext, false);
        }
    }

    private class IndexResource {
        private String serverId;
        private File localCache;
        private File localIndex;

        public IndexResource(String serverId, File localCache, File localIndex) {
            this.serverId = serverId;
            this.localCache = localCache;
            this.localIndex = localIndex;
        }

        public String getServerId() {
            return serverId;
        }

        public File getLocalCache() {
            return localCache;
        }

        public File getLocalIndex() {
            return localIndex;
        }
    }
}
