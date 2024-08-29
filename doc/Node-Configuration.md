# Node Configuration for Stork oracle extension

This extension requires the following node configuration:

| Name             | Description              | Type   | Required           | Default |
|------------------|--------------------------|--------|--------------------|---------|
| `stork.url`      | Stork web socket API URL | string | :white_check_mark: |         |
| `stork.username` | Stork account user name  | string | :white_check_mark: |         |
| `stork.password` | Stork account password   | string | :white_check_mark: |         |

## Configuration when running Master-Sub architecture

To run EIF on Master-Sub architecture you need to add the following properties:

```properties
container.config-providers=net.postchain.stork.config.StorkContainerConfigProvider
```
