# Zabbix Receiver
Zabbix receiver is accepting the metrics of [Zabbix Agent Active Checks protocol](https://www.zabbix.com/documentation/current/manual/appendix/items/activepassive#active_checks) fromat into the [Meter System](./../../concepts-and-designs/meter.md).

## Module define
```yaml
receiver-zabbix:
  selector: ${SW_RECEIVER_METER:default}
  default:
    # Export tcp port, Zabbix agent could connected and transport data
    port: 13800
    # Enable config when receive agent request
    activeFiles: agent.yaml
```

## Configuration file
Zabbix receiver is configured via a configuration file. The configuration file defines everything related to receiving 
 from agents, as well as which rule files to load.
 
OAP can load the configuration at bootstrap. If the new configuration is not well-formed, OAP fails to start up. The files
are located at `$CLASSPATH/zabbix-receive-config`.

The file is written in YAML format, defined by the scheme described below. Brackets indicate that a parameter is optional.

A example can be found [here](../../../../oap-server/server-bootstrap/src/main/resources/meter-analyzer-config/spring-sleuth.yaml).
You could find the Zabbix agent detail items from [Zabbix Agent docucment](https://www.zabbix.com/documentation/current/manual/config/items/itemtypes/zabbix_agent).

### Zabbix agent configure

```yaml
# insert metricPrefix into metric name:  <metricPrefix>_<raw_metric_name>
metricPrefix: <string>
# expSuffix is appended to all expression in this file.
expSuffix: <string>
# Support agent entities information.
entities:
  # Allow hostname patterns to build metrics.
  hostPatterns:
    - <regex string>
  # Customized metrics label before parse to meter system.
  labels:
    [- <labels> ]
# Metrics rule allow you to recompute queries.
metrics:
  [ - <metrics_rules> ]
```

#### <labels>

```yaml
# Define the lable name. The label value must query from `value` or `fromItem` attribute.
name: <string>
# Appoint value to label.
[value: <string>]
# Query label value from Zabbix Agent Item key.
[fromItem: <string>]
```

#### <metric_rules>

```yaml
# The name of rule, which combinates with a prefix 'meter_' as the index/table name in storage.
name: <string>
# MAL expression.
exp: <string>
```

More about MAL, please refer to [mal.md](../../concepts-and-designs/mal.md).