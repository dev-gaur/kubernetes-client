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
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.JSONSchemaProps;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.mock.crd.*;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@EnableRuleMigrationSupport
public class MYCRDTest {
  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  private CustomResourceDefinition customResourceDefinition;

  @BeforeEach
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

  @Test
  public void testCreate() {
//    server.expect().post().withPath("/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions").andReturn(200, customResourceDefinition).once();
//    server.expect().post().withPath("/apis/gaur.dev/v1/namespaces/test/foo-bars").andReturn(200, getFooBar() ).once();
//    server.expect().get().withPath("/apis/gaur.dev/v1/namespaces/test/foo-bars/example").andReturn(200, getFooBar()).times(2);

    KubernetesClient client = server.getClient();

    CustomResourceDefinition fooBarCrd = client.customResourceDefinitions().create(customResourceDefinition);
    assertNotNull(fooBarCrd);
    System.out.println(fooBarCrd);
    assertEquals("foo-bar.gaur.dev", fooBarCrd.getMetadata().getName());
    // Assertion to test behavior in https://github.com/fabric8io/kubernetes-client/issues/1486
    //assertNull(fooBarCrd.getSpec().getValidation().getOpenAPIV3Schema().getDependencies());

    MixedOperation<FooBar, FooBarList, DoneableFooBar, Resource<FooBar, DoneableFooBar>> fooBarClient = client.customResources(customResourceDefinition, FooBar.class, FooBarList.class, DoneableFooBar.class);
    FooBar fb1 = new FooBar();
    fb1.getMetadata().setName("example");
    FooBarSpec fooBarSpec = new FooBarSpec();
    fooBarSpec.setReplicas(5);

    fb1.setSpec(fooBarSpec);
    fb1.setApiVersion("gaur.dev/v1");
    FooBar fb = fooBarClient.create(fb1);

    assertNotNull(fb);

    FooBarList list = fooBarClient.list();

    System.out.println(list.getItems().get(0));
    /*
    FooBar f2 = fooBarClient.withName("example").get();

    assertNotNull(f2);
    System.out.println("KIND: " + f2.getKind());
    assertEquals("FooBar", f2.getKind());
    //assertEquals("", f2.getSpec());
    System.out.println(f2);
    FooBar f3 = fooBarClient.withName("example").get();
*/
  }


  @Test
  public void testGet() {
    server.expect().get().withPath("/apis/gaur.dev/v1/foo-bar/example").andReturn(200, customResourceDefinition).once();
    KubernetesClient client = server.getClient();

//    CustomResourceDefinition crd = client.customResourceDefinitions().withName("sparkclusters.radanalytics.io").get();
    CustomResourceDefinition crd = client.customResourceDefinitions().withName(customResourceDefinition.getMetadata().getName()).get();

    assertNotNull(crd);
    assertEquals("sparkclusters.radanalytics.io", crd.getMetadata().getName());
  }

/*
  @Test
  public void testCreate() {
    server.expect().post().withPath("/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions").andReturn(200, customResourceDefinition).once();
    KubernetesClient client = server.getClient();

    CustomResourceDefinition crd = client.customResourceDefinitions().createOrReplace(customResourceDefinition);
    assertNotNull(crd);
    assertEquals("sparkclusters.radanalytics.io", crd.getMetadata().getName());
    // Assertion to test behavior in https://github.com/fabric8io/kubernetes-client/issues/1486
    assertNull(crd.getSpec().getValidation().getOpenAPIV3Schema().getDependencies());
  }
*/
  @Test
  public void testList() {
    server.expect().get().withPath("/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions").andReturn(200, new KubernetesListBuilder().withItems(customResourceDefinition).build()).once();
    KubernetesClient client = server.getClient();

    CustomResourceDefinitionList crdList = client.customResourceDefinitions().list();
    assertNotNull(crdList);
    assertEquals(1, crdList.getItems().size());
    assertEquals(customResourceDefinition.getMetadata().getName(), crdList.getItems().get(0).getMetadata().getName());


  }
/*
  @Test
  public void testDelete() {
    server.expect().delete().withPath("/apis/apiextensions.k8s.io/v1beta1/customresourcedefinitions/sparkclusters.radanalytics.io").andReturn(200, customResourceDefinition).once();
    KubernetesClient client = server.getClient();

    Boolean deleted = client.customResourceDefinitions().withName("sparkclusters.radanalytics.io").delete();
    assertTrue(deleted);
  }
*/
  public JSONSchemaProps readSchema() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    final URL resource = getClass().getResource("/test-crd-validation-schema.json");

    final JSONSchemaProps jsonSchemaProps = mapper.readValue(resource, JSONSchemaProps.class);
    return jsonSchemaProps;
  }

  private FooBar getFooBar() {
    FooBarSpec fooBarSpec = new FooBarSpec();
    fooBarSpec.setReplicas(5);

    FooBar fooBar = new FooBar();
    fooBar.setApiVersion("gaur.dev/v1");
    fooBar.setMetadata(new ObjectMetaBuilder().withName("example").build());
    fooBar.setSpec(fooBarSpec);
    return fooBar;
  }
}
