/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.maven.indexer.settings;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.DefaultSettingsProblem;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * Maven implementation doesn't allow to set a {@link SecDispatcher} therefore adding this class
 * with the same logic.
 */
class DefaultSettingsDecrypter implements SettingsDecrypter {

  private SecDispatcher securityDispatcher;

  public DefaultSettingsDecrypter(SecDispatcher securityDispatcher) {
    requireNonNull(securityDispatcher, "securityDispatcher cannot be null");
    this.securityDispatcher = securityDispatcher;
  }

  @Override
  public SettingsDecryptionResult decrypt(SettingsDecryptionRequest request) {
    List<SettingsProblem> problems = new ArrayList<>();

    List<Server> servers = new ArrayList<>();

    for (Server server : request.getServers()) {
      server = server.clone();

      servers.add(server);

      try {
        server.setPassword(decrypt(server.getPassword()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt password for server " + server.getId()
            + ": " + e.getMessage(), SettingsProblem.Severity.ERROR,
                                                "server: " + server.getId(), -1, -1, e));
      }

      try {
        server.setPassphrase(decrypt(server.getPassphrase()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt passphrase for server " + server.getId()
            + ": " + e.getMessage(), SettingsProblem.Severity.ERROR,
                                                "server: " + server.getId(), -1, -1, e));
      }
    }

    List<Proxy> proxies = new ArrayList<>();

    for (Proxy proxy : request.getProxies()) {
      proxy = proxy.clone();

      proxies.add(proxy);

      try {
        proxy.setPassword(decrypt(proxy.getPassword()));
      } catch (SecDispatcherException e) {
        problems.add(new DefaultSettingsProblem("Failed to decrypt password for proxy " + proxy.getId()
            + ": " + e.getMessage(), SettingsProblem.Severity.ERROR,
                                                "proxy: " + proxy.getId(), -1, -1, e));
      }
    }

    return new DefaultSettingsDecryptionResult(servers, proxies, problems);
  }

  private String decrypt(String str) throws SecDispatcherException {
    return (str == null) ? null : securityDispatcher.decrypt(str);
  }

}
