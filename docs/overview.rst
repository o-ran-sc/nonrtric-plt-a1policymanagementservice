.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021 Nordix

A1 Policy Management Service (from ONAP CCSDK)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A1 Controller Service above A1 Controller/Adaptor that provides:

* Maintains (persistent) cache of RAN's A1 Policy information.

  * Support RAN-wide view of A1 Policy information.
  * Streamline A1 traffic.
  * Enable (optional) re-synchronization after inconsistencies / near-RT-RIC restarts.
  * Supports a large number of near-RT-RICs (& multi-version support).

* Converged ONAP & O-RAN-SC A1 Adapter/Controller functions in ONAP SDNC/CCSDK (Optionally deploy without A1 Adaptor to connect direct to near-RT-RICs).
* Support for different Southbound connectors per near-RT-RIC - e.g. different A1 versions, different near-RT-RIC version, different A1 adapter/controllers supports different or proprietary A1 controllers/EMSs.

See also A1 Policy Management Service in ONAP: `Wiki <https://wiki.onap.org/pages/viewpage.action?pageId=84672221>`_, :doc:`Documentation<onapa1policymanagementservice:index>`.

Implementation:

* Implemented as a Java Spring Boot application.

This product is a part of :doc:`NONRTRIC <nonrtric:index>`.
