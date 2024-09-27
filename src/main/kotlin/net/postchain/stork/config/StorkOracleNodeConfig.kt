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
        private const val STORK_CONFIG_ENV_PREFIX = "POSTCHAIN_STORK_"
        private const val STORK_URL = "${STORK_CONFIG_ENV_PREFIX}_URL"
        private const val STORK_USERNAME = "${STORK_CONFIG_ENV_PREFIX}_USERNAME"
        private const val STORK_PASSWORD = "${STORK_CONFIG_ENV_PREFIX}_PASSWORD"
        private const val STORK_API_TYPE = "${STORK_CONFIG_ENV_PREFIX}_API_TYPE"

        @JvmStatic
        fun fromAppConfig(config: AppConfig): StorkOracleNodeConfig {
            return StorkOracleNodeConfig(
                    config.getEnvOrString(STORK_URL, "stork.url")
                            ?: throw UserMistake("Stork URL must be configured"),
                    config.getEnvOrString(STORK_USERNAME, "stork.username")
                            ?: throw UserMistake("Stork username must be configured"),
                    config.getEnvOrString(STORK_PASSWORD, "stork.password")
                            ?: throw UserMistake("Stork password must be configured"),
                    config.getEnvOrString(STORK_API_TYPE, "stork.api_type")?.let {
                        StorkApiType.valueOf(it)
                    } ?: StorkApiType.REST
            )
        }
    }

    fun toEnvironmentKeyValueMap(): Map<String, String> = buildMap {
        put(STORK_URL, url)
        put(STORK_USERNAME, username)
        put(STORK_PASSWORD, password)
        put(STORK_API_TYPE, apiType.name)
    }
}
