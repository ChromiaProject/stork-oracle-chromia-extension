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
    rid: {INSERT_HASH}
    insecure: false
```

Extend the price update hook:

```
@extend(on_stork_oracle_prices_update) function handle_price_update(stork_oracle_prices) {
    // Write your code here
}
```

The struct `stork_oracle_prices` struct looks as follows:

```
struct stork_oracle_prices {
    asset: text;
    stork_price;
    publisher_prices: list<publisher_price>;
}

struct stork_price {
    price: big_integer;
    signature;
    timestamp_nanos: integer;
    merkle_root: byte_array;
    type: text;
    version: text;
    checksum: byte_array;
}

struct publisher_price {
    price: big_integer;
    signature;
    timestamp_seconds: integer;
}

struct signature {
    signer: byte_array;
    r: byte_array;
    s: byte_array;
    v: byte_array;
}
```

The extension already verifies the signatures for you so there is no need to do that in your Rell code.

## Custom subnode image
TODO
