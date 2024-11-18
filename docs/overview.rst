.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021-2023 Nordix Foundation
.. Copyright (C) 2024: OpenInfra Foundation Europe. All rights reserved.

O-RAN A1 Interface
~~~~~~~~~~~~~~~~~~

The O-RAN Alliance defines a new management interface "A1" for Radio Access Network (RAN) Management. This interface interconnects a new logical 
"Non-RealTime RAN Intelligent Controller (Non-RT-RIC)" function in the Service Management & Orchestration (SMO) layer with a new logical "Near-RealTime 
RAN Intelligent Controller (near-RT-RIC)" function in the RAN. This new A1 interface allows the Non-RT-RIC to provide Policy Guidance to the RAN (near-RT-RIC) 
to steer its operation. These policies are defined by the O-RAN Alliance as "A1 Policies". The specifications for the A1 Interface, including A1 Policy support, 
can be found on the O-RAN Alliance Specifications website.

The A1 Policy functions are Orchestration and Automation functions for non-real-time intelligent management of RAN functions. The primary goal of the A1 Policy 
functions is to support non-real-time radio resource management, higher layer procedure optimization, policy optimization in RAN, and providing guidance, 
parameters, policies and AI/ML models to support the operation of Near-RealTime RIC (RAN Intelligent Controller) functions in the RAN to achieve 
higher-level non-real-time objectives. 

A1 Policy functions form part of a Non-Realtime RIC as defined by O-RAN Alliance. Non-Realtime RIC functions include service and policy management, RAN analytics, 
and model-training for the Near-RealTime RICs. The ONAP & O-RAN-SC A1 Policy work provides concepts, specifications, architecture and reference implementations 
for A1 Policy support as defined and described in the O-RAN architecture.

A1 Policy Management Service (from ONAP CCSDK)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The A1 Policy Management Service is a microservice which maintains a transient repository of:

    * All A1 policies instances in the network. Each policy is targeted to a near-RT-RIC instance and is owned by a 'service' (e.g., rApps or the NONRTRIC Dashboard).
    * All near-RT-RICs in the network.
    * All Policy types supported by each near-RT-RIC.

The service provides :

    * Unified REST API for managing A1 Policies in all near-RT-RICs.
    * Compliant with O-RAN R1 specification for A1-Policy Management (R1-AP v5.0, with additional features & fixes)
    * Synchronized view of A1 Policy instances for each rAPP
    * Synchronized view of A1 Policy instances in each near-RT-RIC
    * Synchronized view of A1 Policy types supported by each near-RT-RIC
    * Lookup service to find the near-RT-RIC to control resources in the RAN as defined in  O1 (e.g. which near-RT-RIC should be accessed to control a certain CU or DU, which in turn controls a certain cell).
    * Monitors all near-RT-RICs and maintains data consistency, e.g. recovery from near-RT-RIC restarts
    * Support for different Southbound APIs  to the near-RT-RICs (different versions of the A1-P and other similar APIs).
    * HTTPS can be configured to use a supplied certificate/private key and to validate peers towards a list of trusted CAs/certs.
    * HTTP proxy support for tunneling HTTP/HTTPS connections.
    * Fine-grained access-control - with new optional callouts to an external auth function
    * Fine-grained monitoring metrics, logging & call tracing can be configured

See also A1 Policy Management Service in ONAP: `Wiki <https://lf-onap.atlassian.net/wiki/spaces/DW/pages/16444961/O-RAN+A1+Policies+in+ONAP>`_, :doc:`Documentation<onapa1policymanagementservice:index>`.

Implementation:

* Implemented as a Java Spring Boot application.

This product is a part of :doc:`NONRTRIC <nonrtric:index>`.
