package io.fabric8.kubernetes.client.mock.crd;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

/*
 */
@JsonDeserialize(
  using = JsonDeserializer.None.class
)
public class FooBarSpec implements KubernetesResource {
  public int getReplicas() {
    return replicas;
  }

  @Override
  public String toString() {
    return "FooBarSpec{replicas=" + replicas + "}";
  }

  public void setReplicas(int replicas) {
    this.replicas = replicas;
  }

  private int replicas;
}
