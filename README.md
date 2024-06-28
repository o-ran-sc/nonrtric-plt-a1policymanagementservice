# O-RAN-SC Non-RT RIC Policy Agent

The O-RAN Non-RT RIC Policy Agent provides a REST API for the management of policies within the O-RAN architecture. This README provides details on the features, configuration, and running instructions for the Policy Agent.
For detailed API documentation and further information, refer to the NONRTRIC documentation at [NONRTRIC Wiki](https://wiki.o-ran-sc.org/display/RICNR).

## Features

The Policy Agent offers various functionalities to manage and monitor policies and RIC capabilities:

- **Client Supervision**: Monitors clients (R-APPs) to eliminate stray policies in case of client failure.
- **Consistency Monitoring**: Ensures the SMO view of policies and RIC capabilities is consistent with the actual situation in the RICs.
- **Policy Configuration**:
  - Single REST API for all RICs in the network.
  - Query functions to find all policies in a RIC, all policies owned by a service (R-APP), all policies of a specific type, etc.
  - Maps O1 resources (ManagedElement) to the controlling RIC.

## Configuration

The Policy Agent uses default keystore and truststore files included in the container. The paths and passwords for these stores are specified in a YAML configuration file located at:



### Default Truststore Certificates

The default truststore includes the following trusted certificates:
- **A1 Simulator Certificate**: [a1simulator cert](https://gerrit.o-ran-sc.org/r/gitweb?p=sim/a1-interface.git;a=tree;f=near-rt-ric-simulator/certificate;h=172c1e5aacd52d760e4416288dc5648a5817ce65;hb=HEAD)
- **A1 Controller Certificate**: [a1controller cert (keystore.jks)](https://gerrit.o-ran-sc.org/r/gitweb?p=nonrtric.git;a=tree;f=sdnc-a1-controller/oam/installation/sdnc-a1/src/main/resources;h=17fdf6cecc7a866c5ce10a35672b742a9f0c4acf;hb=HEAD)
- **Policy Agent's Own Certificate**: Used for mocking and unit-testing purposes (ApplicationTest.java).

### Overriding Default Configuration

You can override the default keystore, truststore, and application.yaml files by mounting new files using the `volumes` field in Docker Compose or the `docker run` command.

Assuming the new keystore, truststore, and application.yaml files are located in the same directory as your Docker Compose file, the `volumes` field should include these entries:

```yaml
volumes:
  - ./new_keystore.jks:/opt/app/policy-agent/etc/cert/keystore.jks:ro
  - ./new_truststore.jks:/opt/app/policy-agent/etc/cert/truststore.jks:ro
  - ./new_application.yaml:/opt/app/policy-agent/config/application.yaml:ro
```

The target paths in the container should remain unchanged.

Example Docker Run Command
To run the Policy Agent container and mount the new configuration files, use the following docker run command:

```sh
docker run -p 8081:8081 -p 8433:8433 --name=policy-agent-container --network=nonrtric-docker-net \
  --volume "$PWD/new_keystore.jks:/opt/app/policy-agent/etc/cert/keystore.jks" \
  --volume "$PWD/new_truststore.jks:/opt/app/policy-agent/etc/cert/truststore.jks" \
  --volume "$PWD/new_application.yaml:/opt/app/policy-agent/config/application.yaml" \
  o-ran-sc/nonrtric-policy-agent:2.2.0-SNAPSHOT
```

### Running Policy Agent Locally
To run the Policy Agent locally in a simulated test mode, follow these steps:

1. In the folder /opt/app/policy-agent/config/, create a soft link to your test configuration file:

```sh
ln -s <path to test_application_configuration.json> application_configuration.json
```

2. Start the Policy Agent with the following Maven command:

```sh
mvn -Dtest=MockPolicyAgent test
```

This will start the agent in a simulated mode, where it mimics the behavior of RICs. The REST API will be available on port 8081.

### API Documentation
The backend server publishes live API documentation, which can be accessed at:

```bash
http://your-host-name-here:8081/swagger-ui.html
```

## License

Copyright (C) 2019 Nordix Foundation. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
