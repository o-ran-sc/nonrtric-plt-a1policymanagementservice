/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 *  Copyright (C) 2023-2024 OpenInfra Foundation Europe. All rights reserved.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================LICENSE_END===================================
 */

package org.onap.ccsdk.oran.a1policymanagementservice.clients;

import static org.onap.ccsdk.oran.a1policymanagementservice.clients.OscA1Client.extractCreateSchema;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import org.onap.ccsdk.oran.a1policymanagementservice.configuration.RicConfig;
import org.onap.ccsdk.oran.a1policymanagementservice.repository.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Client for accessing OSC A1-Mediator A1-P Version (Release I)
 */
@SuppressWarnings("squid:S2629") // Invoke method(s) only conditionally
public class A1MediatorAdapterI implements A1Client {
    static final int CONCURRENCY_RIC = 1; // How many parallel requests that is sent to one NearRT RIC

    public static class Factory implements A1Client.Factory {
        @Override
        public A1Client create(RicConfig ricConfig, AsyncRestClientFactory restClientFactory) {
            return new A1MediatorAdapterI(ricConfig, restClientFactory);
        }
    }

    public static class A1MediatorRelIUriBuilder extends StdA1ClientVersion2.OranV2UriBuilder{
        public A1MediatorRelIUriBuilder(RicConfig ricConfig) {
            super(ricConfig);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AsyncRestClient restClient;
    private final A1MediatorRelIUriBuilder uriBuilder;

    public A1MediatorAdapterI(RicConfig ricConfig, AsyncRestClientFactory restClientFactory) {
        this(ricConfig, restClientFactory.createRestClientUseHttpProxy(""));
    }

    public A1MediatorAdapterI(RicConfig ricConfig, AsyncRestClient restClient) {
        this.restClient = restClient;
        logger.debug("A1MediatorAdapterI for ric: {}", ricConfig.getRicId());
        uriBuilder = new A1MediatorRelIUriBuilder(ricConfig);
    }

    @Override
    public Mono<List<String>> getPolicyTypeIdentities() {
        return getPolicyTypeIds() //
                .collectList();
    }

    @Override
    public Mono<List<String>> getPolicyIdentities() {
        return getPolicyTypeIds() //
                .flatMap(this::getPolicyIdentitiesByType) //
                .collectList();
    }

    @Override
    public Mono<String> getPolicyTypeSchema(String policyTypeId) {
        String schemaUri = uriBuilder.createGetSchemaUri(policyTypeId);
        return restClient.get(schemaUri) //
                .flatMap(response -> extractCreateSchema(response, policyTypeId));
    }

    @Override
    public Mono<String> putPolicy(Policy policy) {
        String policyUri = this.uriBuilder.createPutPolicyUri(policy.getType().getId(), policy.getId(),
                policy.getStatusNotificationUri());
        return restClient.put(policyUri, policy.getJson());
    }

    @Override
    public Mono<String> deletePolicy(Policy policy) {
        return deletePolicyById(policy.getType().getId(), policy.getId());
    }

    @Override
    public Mono<A1ProtocolType> getProtocolVersion() {
        return Mono.just(A1ProtocolType.CUSTOM_PROTOCOL);
    }

    @Override
    public Flux<String> deleteAllPolicies(Set<String> excludePolicyIds) {
        return getPolicyTypeIds() //
                .flatMap(typeId -> deletePoliciesForType(typeId, excludePolicyIds), CONCURRENCY_RIC);
    }

    @Override
    public Mono<String> getPolicyStatus(Policy policy) {
        String statusUri = uriBuilder.createGetPolicyStatusUri(policy.getType().getId(), policy.getId());
        return restClient.get(statusUri);

    }

    private Flux<String> getPolicyTypeIds() {
        return restClient.get(uriBuilder.createPolicyTypesUri()) //
                .flatMapMany(A1AdapterJsonHelper::parseJsonArrayOfString);
    }

    private Flux<String> getPolicyIdentitiesByType(String typeId) {
        return restClient.get(uriBuilder.createGetPolicyIdsUri(typeId)) //
                .flatMapMany(A1AdapterJsonHelper::parseJsonArrayOfString);
    }

    private Mono<String> deletePolicyById(String typeId, String policyId) {
        String policyUri = uriBuilder.createDeleteUri(typeId, policyId);
        return restClient.delete(policyUri);
    }

    private Flux<String> deletePoliciesForType(String typeId, Set<String> excludePolicyIds) {
        return getPolicyIdentitiesByType(typeId) //
                .filter(policyId -> !excludePolicyIds.contains(policyId)) //
                .flatMap(policyId -> deletePolicyById(typeId, policyId), CONCURRENCY_RIC);
    }
}
