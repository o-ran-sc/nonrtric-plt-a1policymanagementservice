.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021-2023 Nordix
.. Copyright (C) 2024: OpenInfra Foundation Europe. All rights reserved.

===============
Developer Guide
===============

This document provides a quickstart for developers of the Non-RT RIC A1 Policy Management Service.

The A1 Policy Management Service is implemented in ONAP. For additional documentation 
see `ONAP CCSDK documentation <https://docs.onap.org/projects/onap-ccsdk-oran>`_.
and `wiki <https://wiki.onap.org/display/DW/O-RAN+A1+Policies+in+ONAP>`_.

Docker & Kubernetes deployment
==============================

Information on running and configuring the functions in Docker can be found 
on the `Run in Docker wiki page <https://wiki.o-ran-sc.org/display/RICNR/Release+J+-+Run+in+Docker>`_

Non-RT RIC can be also deployed in a Kubernetes cluster, `it/dep repository <https://gerrit.o-ran-sc.org/r/admin/repos/it/dep>`_.
hosts deployment and integration artifacts. Instructions and helm charts to deploy the Non-RT-RIC functions in the
OSC NONRTRIC integrated test environment can be found in the *./nonrtric* directory.

For more information see `Integration and Testing documentation on the O-RAN-SC wiki <https://docs.o-ran-sc.org/projects/o-ran-sc-it-dep>`_.

For more information on installation of NonRT-RIC in Kubernetes, 
see `Deploy NONRTRIC functions in Kubernetes <https://wiki.o-ran-sc.org/display/RICNR/Release+J+-+Run+in+Kubernetes>`_.


