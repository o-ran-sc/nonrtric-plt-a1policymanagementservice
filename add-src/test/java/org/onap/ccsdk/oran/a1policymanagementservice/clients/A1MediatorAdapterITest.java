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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.ccsdk.oran.a1policymanagementservice.configuration.RicConfig;
import org.onap.ccsdk.oran.a1policymanagementservice.repository.Policy;
import org.onap.ccsdk.oran.a1policymanagementservice.repository.PolicyType;
import org.onap.ccsdk.oran.a1policymanagementservice.repository.Ric;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class A1MediatorAdapterITest {


    private static final String RIC_URL = "https://ric.com";

    private static final String RIC_BASE_URL = RIC_URL + "/A1-P/v2";

    private static final String POLICYTYPES_IDENTITIES_URL = RIC_BASE_URL + "/policytypes";
    private static final String POLICIES = "/policies";
    private static final String POLICYTYPES_URL = RIC_BASE_URL + "/policytypes/";
    private static final String POLICY_TYPE_1_ID = "type1";
    private static final String POLICY_TYPE_2_ID = "type2";
    private static final String POLICY_TYPE_SCHEMA_VALID = "{\"type\":\"type1\"}";
    private static final String POLICY_TYPE_SCHEMA_INVALID = "\"type\":\"type1\"}";
    private static final String POLICY_1_ID = "policy1";
    private static final String POLICY_2_ID = "policy2";
    private static final String POLICY_JSON_VALID = "{\"policyId\":\"policy1\"}";

    A1MediatorAdapterI clientUnderTest;

    AsyncRestClient asyncRestClientMock;

    @BeforeEach
    void init() {
        RicConfig ricConfig = RicConfig.builder() //
                .ricId("name") //
                .baseUrl(RIC_URL) //
                .build();
        asyncRestClientMock = mock(AsyncRestClient.class);
        clientUnderTest = new A1MediatorAdapterI(ricConfig, asyncRestClientMock);
    }

    @Test
    @DisplayName("test Get Policy Type Identities")
    void testGetPolicyTypeIdentities() {
        List<String> policyTypeIds = Arrays.asList(POLICY_TYPE_1_ID, POLICY_TYPE_2_ID);
        Mono<String> policyTypeIdsResp = Mono.just(policyTypeIds.toString());
        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeIdsResp);

        Mono<List<String>> returnedMono = clientUnderTest.getPolicyTypeIdentities();
        verify(asyncRestClientMock).get(POLICYTYPES_IDENTITIES_URL);
        StepVerifier.create(returnedMono).expectNext(policyTypeIds).expectComplete().verify();
    }

    @Test
    @DisplayName("test Get Policy Identities")
    void testGetPolicyIdentities() {
        Mono<String> policyTypeIdsResp = Mono.just(Arrays.asList(POLICY_TYPE_1_ID, POLICY_TYPE_2_ID).toString());
        Mono<String> policyIdsType1Resp = Mono.just(Arrays.asList(POLICY_1_ID).toString());
        Mono<String> policyIdsType2Resp = Mono.just(Arrays.asList(POLICY_2_ID).toString());
        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeIdsResp).thenReturn(policyIdsType1Resp)
                .thenReturn(policyIdsType2Resp);

        List<String> returned = clientUnderTest.getPolicyIdentities().block();

        assertEquals(2, returned.size(), "");
        verify(asyncRestClientMock).get(POLICYTYPES_IDENTITIES_URL);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_1_ID + POLICIES);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_2_ID + POLICIES);
    }

    @Test
    @DisplayName("test Get Valid PolicyType")
    void testGetValidPolicyType() {
        String policyType = "{\"create_schema\": " + POLICY_TYPE_SCHEMA_VALID + "}";
        Mono<String> policyTypeResp = Mono.just(policyType);

        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeResp);

        Mono<String> returnedMono = clientUnderTest.getPolicyTypeSchema(POLICY_TYPE_1_ID);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_1_ID);
        StepVerifier.create(returnedMono).expectNext(getCreateSchema(policyType, POLICY_TYPE_1_ID)).expectComplete()
                .verify();
    }

    @Test
    @DisplayName("test Get In Valid Policy Type Json")
    void testGetInValidPolicyTypeJson() {
        String policyType = "{\"create_schema\": " + POLICY_TYPE_SCHEMA_INVALID + "}";
        Mono<String> policyTypeResp = Mono.just(policyType);

        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeResp);

        Mono<String> returnedMono = clientUnderTest.getPolicyTypeSchema(POLICY_TYPE_1_ID);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_1_ID);
        StepVerifier.create(returnedMono).expectErrorMatches(throwable -> throwable instanceof JSONException).verify();
    }

    @Test
    @DisplayName("test Get Policy Type Without CreateS chema")
    void testGetPolicyTypeWithoutCreateSchema() {
        Mono<String> policyTypeResp = Mono.just(POLICY_TYPE_SCHEMA_VALID);

        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeResp);

        Mono<String> returnedMono = clientUnderTest.getPolicyTypeSchema(POLICY_TYPE_1_ID);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_1_ID);
        StepVerifier.create(returnedMono).expectErrorMatches(throwable -> throwable instanceof Exception).verify();
    }

    @Test
    @DisplayName("test Put Policy")
    void testPutPolicy() {
        when(asyncRestClientMock.put(anyString(), anyString())).thenReturn(Mono.empty());

        clientUnderTest
                .putPolicy(createPolicy(RIC_URL, POLICY_1_ID, POLICY_JSON_VALID, POLICY_TYPE_1_ID))
                .block();

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(asyncRestClientMock).put(urlCaptor.capture(), eq(POLICY_JSON_VALID));
        String actualUrl = urlCaptor.getValue();
        String expUrl = POLICYTYPES_URL + POLICY_TYPE_1_ID + POLICIES + "/" + POLICY_1_ID;
        assertThat(actualUrl).contains(expUrl);
    }

    @Test
    @DisplayName("test Delete Policy")
    void testDeletePolicy() {
        when(asyncRestClientMock.delete(anyString())).thenReturn(Mono.empty());

        Mono<String> returnedMono = clientUnderTest
                .deletePolicy(createPolicy(RIC_URL, POLICY_1_ID, POLICY_JSON_VALID, POLICY_TYPE_1_ID));
        verify(asyncRestClientMock).delete(POLICYTYPES_URL + POLICY_TYPE_1_ID + POLICIES + "/" + POLICY_1_ID);
        StepVerifier.create(returnedMono).expectComplete().verify();
    }

    @Test
    @DisplayName("test Delete All Policies")
    void testDeleteAllPolicies() {
        Mono<String> policyTypeIdsResp = Mono.just(Arrays.asList(POLICY_TYPE_1_ID, POLICY_TYPE_2_ID).toString());
        Mono<String> policyIdsType1Resp = Mono.just(Arrays.asList(POLICY_1_ID).toString());
        Mono<String> policyIdsType2Resp = Mono.just(Arrays.asList(POLICY_2_ID).toString());
        when(asyncRestClientMock.get(anyString())).thenReturn(policyTypeIdsResp).thenReturn(policyIdsType1Resp)
                .thenReturn(policyIdsType2Resp);
        when(asyncRestClientMock.delete(anyString())).thenReturn(Mono.empty());

        Flux<String> returnedFlux = clientUnderTest.deleteAllPolicies();
        StepVerifier.create(returnedFlux).expectComplete().verify();
        verify(asyncRestClientMock).get(POLICYTYPES_IDENTITIES_URL);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_1_ID + POLICIES);
        verify(asyncRestClientMock).delete(POLICYTYPES_URL + POLICY_TYPE_1_ID + POLICIES + "/" + POLICY_1_ID);
        verify(asyncRestClientMock).get(POLICYTYPES_URL + POLICY_TYPE_2_ID + POLICIES);
        verify(asyncRestClientMock).delete(POLICYTYPES_URL + POLICY_TYPE_2_ID + POLICIES + "/" + POLICY_2_ID);
    }

    private String getCreateSchema(String policyType, String policyTypeId) {
        JSONObject obj = new JSONObject(policyType);
        JSONObject schemaObj = obj.getJSONObject("create_schema");
        schemaObj.put("title", policyTypeId);
        return schemaObj.toString();
    }

    private static Ric createRic(String url) {
        RicConfig cfg = RicConfig.builder().ricId("ric") //
                .baseUrl(url) //
                .managedElementIds(new Vector<String>(Arrays.asList("kista_1", "kista_2"))) //
                .build();
        return new Ric(cfg);
    }

    private static Policy createPolicy(String nearRtRicUrl, String policyId, String json, String type) {
        String callbackUrl = "https://test.com";
        return Policy.builder() //
                .id(policyId) //
                .json(json) //
                .ownerServiceId("service") //
                .ric(createRic(nearRtRicUrl)) //
                .type(createPolicyType(type)) //
                .lastModified(Instant.now()) //
                .isTransient(false) //
                .statusNotificationUri(callbackUrl) //
                .build();
    }

    private static PolicyType createPolicyType(String name) {
        return PolicyType.builder().id(name).schema("schema").build();
    }
}
