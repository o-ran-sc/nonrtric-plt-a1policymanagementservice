# O-RAN-SC Non-RT RIC A1 Policy Management Service

The O-RAN-SC A1 Policy Management Service provides a REST API for the management of policies within the O-RAN architecture. This README provides details on the features, configuration, and running instructions for the A1 Policy Management Service.
For detailed API documentation and further information, refer to the NONRTRIC documentation at [NONRTRIC Wiki](https://wiki.o-ran-sc.org/display/RICNR).
For additional detailed documentation, also refer to the NONRTRIC documentation at [A1 Policy Management Service Documentation site](https://docs.o-ran-sc.org/projects/o-ran-sc-nonrtric-plt-a1policymanagementservice).

The A1 Policy Management Service is homed in ONAP. For additional documentation see [ONAP CCSDK documentation](https://docs.onap.org/projects/onap-ccsdk-oran).
and [wiki](https://wiki.onap.org/display/DW/O-RAN+A1+Policies+in+ONAP).

## Features


The A1 Policy Management Service is a microservice which maintains a transient repository of:

- All A1 policies instances in the network. Each policy is targeted to a near-RT-RIC instance and is owned by a 'service' (e.g., rApps or the NONRTRIC Dashboard).
- All near-RT-RICs in the network.
- All Policy types supported by each near-RT-RIC.

The service provides :

- Unified REST API for managing A1 Policies in all near-RT-RICs.
- Compliant with O-RAN R1 specification for A1-Policy Management (R1-AP v5.0, with additional features & fixes)
- Synchronized view of A1 Policy instances for each rAPP
- Synchronized view of A1 Policy instances in each near-RT-RIC
- Synchronized view of A1 Policy types supported by each near-RT-RIC
- Lookup service to find the near-RT-RIC to control resources in the RAN as defined in  O1 (e.g. which near-RT-RIC should be accessed to control a certain CU or DU, which in turn controls a certain cell).
- Monitors all near-RT-RICs and maintains data consistency, e.g. recovery from near-RT-RIC restarts
- Support for different Southbound APIs  to the near-RT-RICs (different versions of the A1-P and other similar APIs).
- HTTPS can be configured to use a supplied certificate/private key and to validate peers towards a list of trusted CAs/certs.
- HTTP proxy support for tunneling HTTP/HTTPS connections.
- Fine-grained access-control - with new optional callouts to an external auth function
- Fine-grained monitoring metrics, logging & call tracing can be configured

## Configuration

The A1 Policy Management Service uses default keystore and truststore files included in the container. The paths and passwords for these stores are specified in a YAML configuration file located at:



### Default Truststore Certificates

The default truststore includes the following trusted certificates:
- **A1 Simulator Certificate**: [a1simulator cert](https://gerrit.o-ran-sc.org/r/gitweb?p=sim/a1-interface.git;a=tree;f=near-rt-ric-simulator/certificate)
- **A1 Policy Management Service's Own Certificate**: Used for mocking and unit-testing purposes (ApplicationTest.java).

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
  nexus3.o-ran-sc.org:10002/o-ran-sc/nonrtric-plt-a1policymanagementservice:2.8.0
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

Copyright (C) 2019-2023 Nordix Foundation. All rights reserved.
Copyright (C) 2024: OpenInfra Foundation Europe. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
