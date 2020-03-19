package io.fabric8.kubernetes.client.mock.crd;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class FooBar extends CustomResource {
    private FooBarSpec spec;


    @Override
    public String toString() {
      return "FooBar{" +
        "apiVersion='" + getApiVersion() + '\'' +
        ", metadata=" + getMetadata() +
        ", spec=" + spec +
        '}';
    }

    public FooBarSpec getSpec() {
      return spec;
    }

    public void setSpec(FooBarSpec spec) {
      this.spec = spec;
    }

    @Override
    public ObjectMeta getMetadata() { return super.getMetadata(); }
}

