# Stork Oracle Chromia Extension

Extension for integrating Postchain with Stork price oracle: https://www.stork.network/

## Registration

```shell
pmc subnode-image add --name stork_oracle_chromia_extension \
  --url registry.gitlab.com/chromaway/core/stork-oracle-chromia-extension/chromaway/stork-oracle-extension-chromia-subnode \
  --digest sha256:d468637677b22305020b7e6d8f8cf861d8955345772356007e5eed7b83a5a402 \
  --image-description "Extensions to Postchain for integration against Stork services" \
  -gtx net.postchain.stork.StorkOracleGTXModule
```

This will generate a proposal which need to be voted on.


[Documentation for node providers](doc/Node-Configuration.md)

[Documentation for dApp developers](doc/User-Guide.md)

## Updating the `chromia-subnode` base image

The base image is pinned by both tag and digest in the root `pom.xml` (the `<from><image>` element of the `jib-maven-plugin` configuration). The digest pin ensures reproducible builds; the version tag is kept alongside it for human readability.

To bump the base image (replace `<NEW_VERSION>` with the target tag):

```shell
docker pull registry.gitlab.com/chromaway/postchain-chromia/chromaway/chromia-subnode:<NEW_VERSION>
docker inspect --format='{{index .RepoDigests 0}}' \
  registry.gitlab.com/chromaway/postchain-chromia/chromaway/chromia-subnode:<NEW_VERSION>
```

Copy the resulting `sha256:…` digest and update the `<image>` line in `pom.xml` to:

```
registry.gitlab.com/chromaway/postchain-chromia/chromaway/chromia-subnode:<NEW_VERSION>@<DIGEST>
```
