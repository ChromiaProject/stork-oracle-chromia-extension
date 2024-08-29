# Stork oracle extension user guide

## Blockchain configuration

You will need to configure which assets to listen for updates to in your blockchain configuration as well as the 
necessary Stork extensions:

```yaml
config:
  gtx:
    modules:
      - "net.postchain.stork.StorkOracleGTXModule"
  sync_ext:
    - "net.postchain.stork.StorkOracleSynchronizationInfrastructureExtension"
  stork:
    assets:
      - "BTCUSD"
      - "ETHUSD"
      - ...
```

## Rell

Install the Rell library:

```yaml
libs:
  stork:
    registry: https://gitlab.com/chromaway/core/stork-oracle-chromia-extension
    path: rell/src/stork
    tagOrBranch: {INSERT_VERSION}
    rid: x"DBDC6C8FEC56DB11086FA833E232529AFEB28C379AA84CC9C5A72F5EBFBA6957"
    insecure: false
```

Extend the price update hook:


## Custom subnode image
TODO
