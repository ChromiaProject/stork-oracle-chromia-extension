package net.postchain.stork.config

import net.postchain.config.app.AppConfig
import net.postchain.containers.bpm.ContainerConfigProvider

class StorkContainerConfigProvider : ContainerConfigProvider {
    override fun getConfig(appConfig: AppConfig): Map<String, String> =
            StorkOracleNodeConfig.fromAppConfig(appConfig).toEnvironmentKeyValueMap()
}
