# Stork Oracle Chromia Extension

Extension for integrating Postchain with Stork price oracle: https://www.stork.network/

## Registration

```shell
pmc subnode-image add --name stork_oracle_chromia_extension \
  --url registry.gitlab.com/chromaway/core/stork-oracle-chromia-extension/chromaway/stork-oracle-extension-chromia-subnode \
  --digest sha256:d468637677b22305020b7e6d8f8cf861d8955345772356007e5eed7b83a5a402 \
  --image-description "Extensions to Postchain for integration against Stork services" \
  -gtx net.postchain.stork.StorkOracleGTXModule \
  -sync net.postchain.stork.StorkOracleSynchronizationInfrastructureExtension
```

This will generate a proposal which need to be voted on.


[Documentation for node providers](doc/Node-Configuration.md)

[Documentation for dApp developers](doc/User-Guide.md)
