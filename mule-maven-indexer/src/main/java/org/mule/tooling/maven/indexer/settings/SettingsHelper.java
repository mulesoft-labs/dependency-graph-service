package org.mule.tooling.maven.indexer.settings;

import static java.lang.System.getProperties;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.File;
import java.util.Optional;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;

public class SettingsHelper {

  public static Optional<Settings> getMavenSettings(final Optional<File> secureSettingsFile, Optional<File> userSettingsFile,
                                              Optional<File> globalSettingsFile) {
    if (!userSettingsFile.isPresent() && !globalSettingsFile.isPresent()) {
      return empty();
    }
    try {
      SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
      settingsBuildingRequest.setSystemProperties(getProperties());
      userSettingsFile.ifPresent(settingsBuildingRequest::setUserSettingsFile);
      globalSettingsFile.ifPresent(settingsBuildingRequest::setGlobalSettingsFile);

      DefaultSettingsBuilderFactory mvnSettingBuilderFactory = new DefaultSettingsBuilderFactory();
      DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
      SettingsBuildingResult settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);

      Settings settings = settingsBuildingResult.getEffectiveSettings();

      secureSettingsFile.ifPresent(secureSettings -> {
        DefaultSettingsDecrypter settingsDecrypter =
                MavenClientSettingsDecryptorFactory.newInstance(secureSettings.getAbsolutePath());

        SettingsDecryptionResult result = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(settings));
        settings.setServers(result.getServers());
        settings.setProxies(result.getProxies());
      });

      return of(settings);
    } catch (SettingsBuildingException e) {
      throw new RuntimeException(e);
    }
  }

  static class MavenClientSecDispatcher extends DefaultSecDispatcher {

    public MavenClientSecDispatcher(String configurationFilePath) {
      _configurationFile = configurationFilePath;
      try {
        _cipher = new DefaultPlexusCipher();
      } catch (PlexusCipherException e) {
        throw new IllegalStateException(e);
      }
    }

  }

  static class MavenClientSettingsDecryptorFactory {

    public static DefaultSettingsDecrypter newInstance(String secureSettingsFile) {
      MavenClientSecDispatcher secDispatcher = new MavenClientSecDispatcher(secureSettingsFile);

      DefaultSettingsDecrypter decrypter = new DefaultSettingsDecrypter(secDispatcher);
      return decrypter;
    }
  }

}
