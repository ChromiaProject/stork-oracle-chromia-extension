# Stork Oracle Chromia Extension

Extension for integrating Postchain with Stork price oracle: https://www.stork.network/

## Registration

```shell
pmc subnode-image add --name stork-oracle-chromia-extension \
  --url registry.gitlab.com/chromaway/core/stork-oracle-chromia-extension/chromaway/stork-oracle-extension-chromia-subnode \
  --digest sha256:7729d9f5f16fa0e09ff7c0446230a78b9bfef54b02a6d441a8aa989e6867cbcb \
  --image-description "Extensions to Postchain for integration against Stork services" \
  -gtx net.postchain.stork.StorkOracleGTXModule \
  -sync net.postchain.stork.StorkOracleSynchronizationInfrastructureExtension
```

This will generate a proposal which need to be voted on.


[Documentation for node providers](doc/Node-Configuration.md)

[Documentation for dApp developers](doc/User-Guide.md)
