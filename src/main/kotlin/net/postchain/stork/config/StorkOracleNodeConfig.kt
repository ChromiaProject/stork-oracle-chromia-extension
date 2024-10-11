package net.postchain.stork.config

import net.postchain.common.exception.UserMistake
import net.postchain.config.app.AppConfig

enum class StorkApiType {
    REST, WEBSOCKET
}

data class StorkOracleNodeConfig(
        val url: String,
        val username: String,
        val password: String,
        val apiType: StorkApiType
) {
    companion object {
        private const val STORK_CONFIG_ENV_PREFIX = "POSTCHAIN_EXTENSION_STORK_"
        private const val STORK_URL = "${STORK_CONFIG_ENV_PREFIX}URL"
        private const val STORK_USERNAME = "${STORK_CONFIG_ENV_PREFIX}USERNAME"
        private const val STORK_PASSWORD = "${STORK_CONFIG_ENV_PREFIX}PASSWORD"
        private const val STORK_API_TYPE = "${STORK_CONFIG_ENV_PREFIX}API_TYPE"

        @JvmStatic
        fun fromAppConfig(config: AppConfig): StorkOracleNodeConfig {
            return StorkOracleNodeConfig(
                    config.getEnvOrString(STORK_URL, "extension.stork.url")
                            ?: throw UserMistake("Stork URL must be configured"),
                    config.getEnvOrString(STORK_USERNAME, "extension.stork.username")
                            ?: throw UserMistake("Stork username must be configured"),
                    config.getEnvOrString(STORK_PASSWORD, "extension.stork.password")
                            ?: throw UserMistake("Stork password must be configured"),
                    config.getEnvOrString(STORK_API_TYPE, "extension.stork.api_type")?.let {
                        StorkApiType.valueOf(it)
                    } ?: StorkApiType.REST
            )
        }
    }
}
