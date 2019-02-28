/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.maven.indexer.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

/**
 * Default implementation for {@link SettingsDecryptionResult}.
 */
class DefaultSettingsDecryptionResult implements SettingsDecryptionResult {

  private List<Server> servers;

  private List<Proxy> proxies;

  private List<SettingsProblem> problems;

  public DefaultSettingsDecryptionResult(List<Server> servers, List<Proxy> proxies, List<SettingsProblem> problems) {
    this.servers = (servers != null) ? servers : new ArrayList<>();
    this.proxies = (proxies != null) ? proxies : new ArrayList<>();
    this.problems = (problems != null) ? problems : new ArrayList<>();
  }

  @Override
  public Server getServer() {
    return servers.isEmpty() ? null : servers.get(0);
  }

  @Override
  public List<Server> getServers() {
    return servers;
  }

  @Override
  public Proxy getProxy() {
    return proxies.isEmpty() ? null : proxies.get(0);
  }

  @Override
  public List<Proxy> getProxies() {
    return proxies;
  }

  @Override
  public List<SettingsProblem> getProblems() {
    return problems;
  }

}
