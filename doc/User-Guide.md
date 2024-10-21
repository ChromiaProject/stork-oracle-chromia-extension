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

If necessary, Stork public key and publisher public keys can be overridden via configuration.

```yaml
config:
  stork:
    stork_pubkey: x"0a803F9b1CCe32e2773e0d2e98b37E0775cA5d44"
    publisher_pubkeys: # List ALL publisher keys here if you want to override
      - x"5c946686b0302be54d85394015a9f9fa0952984e"
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

The struct `stork_oracle_prices` looks as follows:

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

Prices are represented as `big_integer` with 18 digits after the decimal point. You can use the following library
function as convenience for converting the price to decimal:

```
function convert_price_to_decimal(price: big_integer): decimal
```

## Custom subnode image

Ensure you pick the Stork custom image when leasing your container.
