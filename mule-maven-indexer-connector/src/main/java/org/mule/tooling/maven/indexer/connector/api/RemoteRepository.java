package org.mule.tooling.maven.indexer.connector.api;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

public class RemoteRepository {
  @Parameter
  private String id;
  @Parameter
  private String url;
  @Parameter
  @Optional
  private String username;
  @Parameter
  @Optional
  private String password;

  public RemoteRepository() {
    // no op
  }

  public RemoteRepository(String id, String url, String username, String password) {
    this.id = id;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
