/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.client.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.admissionregistration.MutatingWebhookBuilder;
import io.fabric8.kubernetes.api.model.admissionregistration.MutatingWebhookConfiguration;
import io.fabric8.kubernetes.api.model.admissionregistration.MutatingWebhookConfigurationBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.JSONSchemaProps;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.mock.crd.DoneableFooBar;
import io.fabric8.kubernetes.client.mock.crd.FooBar;
import io.fabric8.kubernetes.client.mock.crd.FooBarList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableRuleMigrationSupport
public class MutatingWebhookConfigurationTest {
  @Rule
  public KubernetesServer server = new KubernetesServer();

  @Test
  public void create() {
    MutatingWebhookConfiguration mutatingWebhookConfiguration = new MutatingWebhookConfigurationBuilder()
      .withNewMetadata().withName("mutatingWebhookConfiguration1").endMetadata()
      .addToWebhooks(new MutatingWebhookBuilder()
        .withName("webhook1")
        .withNewClientConfig()
        .withNewService()
        .withName("svc1")
        .withNamespace("test")
        .withPath("/mutate")
        .endService()
        .endClientConfig()
        .build())
      .build();

    server.expect().post().withPath("/apis/admissionregistration.k8s.io/v1beta1/mutatingwebhookconfigurations").andReturn(201, mutatingWebhookConfiguration).once();

    KubernetesClient client = server.getClient();
    HasMetadata response = client.resource(mutatingWebhookConfiguration).createOrReplace();
    assertEquals(mutatingWebhookConfiguration, response);
  }


  private CustomResourceDefinition customResourceDefinition;

  @Before
  public void setupCrd() throws IOException {
    customResourceDefinition = new CustomResourceDefinitionBuilder()
      .withApiVersion("apiextensions.k8s.io/v1beta1")
      .withNewMetadata().withName("foo-bar.gaur.dev")
      .endMetadata()
      .withNewSpec()
      .withNewNames()
      .withKind("FooBar")
      .withPlural("foo-bars")
      .withSingular("foo-bar")
      .endNames()
      .withGroup("gaur.dev")
      .withVersion("v1")
      .withScope("Namespaced")
      .withNewValidation()
      .withNewOpenAPIV3SchemaLike(readSchema())
      .endOpenAPIV3Schema()
      .endValidation()
      .endSpec()
      .build();
  }

  public JSONSchemaProps readSchema() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    final URL resource = getClass().getResource("/test-crd-validation-schema.json");

    final JSONSchemaProps jsonSchemaProps = mapper.readValue(resource, JSONSchemaProps.class);
    return jsonSchemaProps;
  }

  @org.junit.Test
  public void testCreate() {
    server.expect().post().withPath("/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions").andReturn(200, customResourceDefinition).once();
    KubernetesClient client = server.getClient();

    CustomResourceDefinition fooBarCrd = client.customResourceDefinitions().createOrReplace(customResourceDefinition);
    assertNotNull(fooBarCrd);
    System.out.println(fooBarCrd);
    Assert.assertEquals("foo-bar.gaur.dev", fooBarCrd.getMetadata().getName());
    // Assertion to test behavior in https://github.com/fabric8io/kubernetes-client/issues/1486
    assertNull(fooBarCrd.getSpec().getValidation().getOpenAPIV3Schema().getDependencies());

    NonNamespaceOperation<FooBar, FooBarList, DoneableFooBar, Resource<FooBar, DoneableFooBar>> fooBarClient = client.customResources(fooBarCrd, FooBar.class, FooBarList.class, DoneableFooBar.class);

    FooBar fb1 = new FooBar();
    fb1.getMetadata().setName("example");

    fooBarClient = ((MixedOperation<FooBar, FooBarList, DoneableFooBar, Resource<FooBar, DoneableFooBar>>) fooBarClient).inNamespace("test");
    fooBarClient.create(fb1);

    FooBar f2 = fooBarClient.withName("example").get();

    System.out.println(f2);
  }
}
