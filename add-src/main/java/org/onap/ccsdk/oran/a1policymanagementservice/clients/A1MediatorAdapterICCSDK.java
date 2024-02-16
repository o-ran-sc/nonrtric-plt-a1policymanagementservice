/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 *  Copyright (C) 2024 OpenInfra Foundation Europe. All rights reserved.
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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;

import org.json.JSONObject;
import org.onap.ccsdk.oran.a1policymanagementservice.clients.A1MediatorAdapterI.A1MediatorRelIUriBuilder;
import org.onap.ccsdk.oran.a1policymanagementservice.configuration.ControllerConfig;
import org.onap.ccsdk.oran.a1policymanagementservice.configuration.RicConfig;
import org.onap.ccsdk.oran.a1policymanagementservice.repository.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Client for accessing the A1 adapter in the CCSDK in ONAP using custom protocol defined in A1MediatorAdapterI.
 */
@SuppressWarnings("squid:S2629") // Invoke method(s) only conditionally
public class A1MediatorAdapterICCSDK implements A1Client {

    static final int CONCURRENCY_RIC = 1; // How many parallel requests that is sent to one NearRT RIC

    static com.google.gson.Gson gson = new GsonBuilder() //
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES) //
            .create(); //

    @Getter
    public static class AdapterRequest {
        private String nearRtRicUrl = null;
        private String body = null;

        public AdapterRequest(String url, String body) {
            this.nearRtRicUrl = url;
            this.body = body;
        }

        public AdapterRequest() {}
    }

    @Getter
    public static class AdapterOutput {
        private String body = null;
        private int httpStatus = 0;

        public AdapterOutput(int status, String body) {
            this.httpStatus = status;
            this.body = body;
        }

        public AdapterOutput() {}
    }

    private static final String GET_POLICY_RPC = "getA1Policy";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AsyncRestClient restClient;
    private final RicConfig ricConfig;
    private final A1MediatorRelIUriBuilder uriBuilder;


    /**
     * Constructor that creates the REST client to use.
     *
     * @param ricConfig the configuration of the Near-RT RIC to communicate
     *        with
     * @param restClientFactory the factory for creating the REST Client
     *
     * @throws IllegalArgumentException when the protocolType is wrong.
     */
    public A1MediatorAdapterICCSDK(RicConfig ricConfig,
            AsyncRestClientFactory restClientFactory) {
        this(A1ProtocolType.CUSTOM_PROTOCOL, ricConfig, restClientFactory
                .createRestClientNoHttpProxy(ricConfig.getControllerConfig().getBaseUrl() + "/rests/operations"));
    }

    /**
     * Constructor where the REST client to use is provided.
     *
     * @param protocolType the southbound protocol of the controller
     * @param ricConfig the configuration of the Near-RT RIC to communicate
     *        with
     * @param restClient the REST client to use
     *
     * @throws IllegalArgumentException when the protocolType is illegal.
     */
    A1MediatorAdapterICCSDK(A1ProtocolType protocolType, RicConfig ricConfig, AsyncRestClient restClient) {
        if (A1ProtocolType.CUSTOM_PROTOCOL.equals(protocolType)) {
            this.restClient = restClient;
            this.ricConfig = ricConfig;
            this.uriBuilder = new A1MediatorAdapterI.A1MediatorRelIUriBuilder(ricConfig);
            logger.debug("CcsdkA1AdapterClient for ric: {}, a1Controller: {}", ricConfig.getRicId(),
                    ricConfig.getControllerConfig());
        } else {
            logger.error("Not supported protocoltype: {}", protocolType);
            throw new IllegalArgumentException("Not handled protocolversion: " + protocolType);
        }
    }

    @Override
    public Mono<List<String>> getPolicyTypeIdentities() {
        return post(GET_POLICY_RPC, uriBuilder.createPolicyTypesUri(), Optional.empty()) //
                .flatMapMany(A1AdapterJsonHelper::parseJsonArrayOfString) //
                .collectList();
    }

    @Override
    public Mono<List<String>> getPolicyIdentities() {
        return getPolicyIds() //
                .collectList();
    }

    @Override
    public Mono<String> getPolicyTypeSchema(String policyTypeId) {
        final String ricUrl = uriBuilder.createGetSchemaUri(policyTypeId);
        return post(GET_POLICY_RPC, ricUrl, Optional.empty()) //
                .flatMap(response -> extractCreateSchema(response, policyTypeId));
    }

    @Override
    public Mono<String> putPolicy(Policy policy) {
        String ricUrl = uriBuilder.createPutPolicyUri(policy.getType().getId(), policy.getId(),
                policy.getStatusNotificationUri());
        return post("putA1Policy", ricUrl, Optional.of(policy.getJson()));
    }

    @Override
    public Mono<String> deletePolicy(Policy policy) {
        return deletePolicyById(policy.getType().getId(), policy.getId());
    }

    @Override
    public Flux<String> deleteAllPolicies(Set<String> excludePolicyIds) {
        return getPolicyTypeIdentities() //
                .flatMapMany(Flux::fromIterable) //
                .flatMap(type -> deleteAllInstancesForType(uriBuilder, type, excludePolicyIds), CONCURRENCY_RIC);
    }

    private Flux<String> getInstancesForType(A1UriBuilder uriBuilder, String type) {
        return post(GET_POLICY_RPC, uriBuilder.createGetPolicyIdsUri(type), Optional.empty()) //
                .flatMapMany(A1AdapterJsonHelper::parseJsonArrayOfString);
    }

    private Flux<String> deleteAllInstancesForType(A1UriBuilder uriBuilder, String type, Set<String> excludePolicyIds) {
        return getInstancesForType(uriBuilder, type) //
                .filter(policyId -> !excludePolicyIds.contains(policyId)) //
                .flatMap(policyId -> deletePolicyById(type, policyId), CONCURRENCY_RIC);
    }

    @Override
    public Mono<A1ProtocolType> getProtocolVersion() {
        return Mono.just(A1ProtocolType.CUSTOM_PROTOCOL);
    }

    @Override
    public Mono<String> getPolicyStatus(Policy policy) {
        String ricUrl = uriBuilder.createGetPolicyStatusUri(policy.getType().getId(), policy.getId());
        return post("getA1PolicyStatus", ricUrl, Optional.empty());
    }

    private Flux<String> getPolicyIds() {
        return getPolicyTypeIdentities() //
                .flatMapMany(Flux::fromIterable)
                .flatMap(type -> post(GET_POLICY_RPC, uriBuilder.createGetPolicyIdsUri(type), Optional.empty())) //
                .flatMap(A1AdapterJsonHelper::parseJsonArrayOfString);
    }

    private Mono<String> deletePolicyById(String type, String policyId) {
        String ricUrl = uriBuilder.createDeleteUri(type, policyId);
        return post("deleteA1Policy", ricUrl, Optional.empty());
    }

    private Mono<String> post(String rpcName, String ricUrl, Optional<String> body) {
        AdapterRequest inputParams = new AdapterRequest(ricUrl, body.isPresent() ? body.get() : null);

        final String inputJsonString = A1AdapterJsonHelper.createInputJsonString(inputParams);
        logger.debug("POST inputJsonString = {}", inputJsonString);
        ControllerConfig controllerConfig = this.ricConfig.getControllerConfig();
        return restClient
                .postWithAuthHeader(controllerUrl(rpcName), inputJsonString, controllerConfig.getUserName(),
                        controllerConfig.getPassword()) //
                .flatMap(resp -> extractResponseBody(resp, ricUrl));
    }

    private Mono<String> extractResponse(JSONObject responseOutput, String ricUrl) {
        AdapterOutput output = gson.fromJson(responseOutput.toString(), AdapterOutput.class);

        String body = output.body == null ? "" : output.body;
        if (HttpStatus.valueOf(output.httpStatus).is2xxSuccessful()) {
            return Mono.just(body);
        } else {
            logger.debug("Error response: {} {}, from: {}", output.httpStatus, body, ricUrl);
            byte[] responseBodyBytes = body.getBytes(StandardCharsets.UTF_8);
            HttpStatus httpStatus = HttpStatus.valueOf(output.httpStatus);
            WebClientResponseException responseException = new WebClientResponseException(httpStatus.value(),
                    httpStatus.getReasonPhrase(), null, responseBodyBytes, StandardCharsets.UTF_8, null);

            return Mono.error(responseException);
        }
    }

    private Mono<String> extractResponseBody(String responseStr, String ricUrl) {
        return A1AdapterJsonHelper.getOutput(responseStr) //
                .flatMap(responseOutput -> extractResponse(responseOutput, ricUrl));
    }

    private String controllerUrl(String rpcName) {
        return "/A1-ADAPTER-API:" + rpcName;
    }
}
