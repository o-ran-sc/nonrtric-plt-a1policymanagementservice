.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021-2023 Nordix Foundation
.. Copyright (C) 2024-2025: OpenInfra Foundation Europe. All rights reserved.

Installation Guide
==================

Abstract
--------

This document describes how to install the Non-RT RIC components, their dependencies and required system resources.

Software Installation and Deployment
------------------------------------

Install with Docker
+++++++++++++++++++

Information on running and configuring the functions in Docker can be found 
on the `Run in Docker wiki page <https://lf-o-ran-sc.atlassian.net/wiki/spaces/RICNR/pages/86802677/Release+K+-+Run+in+Docker>`_

Install with Helm
+++++++++++++++++

Information on running and configuring the functions in Kubernetes can be found 
on the `Run in Kubernetes wiki page <https://lf-o-ran-sc.atlassian.net/wiki/spaces/RICNR/pages/86802787/Release+K+-+Run+in+Kubernetes>`_

Helm charts and an example recipe are provided in the `it/dep repo <https://gerrit.o-ran-sc.org/r/admin/repos/it/dep>`_,
under "nonrtric". By modifying the variables named "installXXX" in the beginning of the example recipe file, which
components that will be installed can be controlled. Then the components can be installed and started by running the
following command:

      .. code-block:: bash

        bin/deploy-nonrtric -f nonrtric/RECIPE_EXAMPLE/example_recipe.yaml
