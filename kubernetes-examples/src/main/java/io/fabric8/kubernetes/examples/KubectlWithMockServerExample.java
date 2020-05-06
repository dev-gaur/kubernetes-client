package io.fabric8.kubernetes.examples;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubectlWithMockServerExample {

  private static final Logger logger = LoggerFactory.getLogger(KubectlWithMockServerExample.class);

  public static void main(String[] args) {
    KubernetesServer server = new KubernetesServer(true, true, true);
    server.init(6443);

    System.out.println("Listening at port 6443");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          Thread.sleep(200);
          System.out.println("Shutting down ...");
          //some cleaning up code...
          server.shutdown();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          e.printStackTrace();
        }
      }
    });
  }



}
