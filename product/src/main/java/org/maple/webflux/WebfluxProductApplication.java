package org.maple.webflux;

import org.maple.webflux.core.CoreConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@SpringBootApplication
@EnableTransactionManagement
@Import(CoreConfiguration.class)
public class WebfluxProductApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebfluxProductApplication.class, args);
  }

  @Bean
  @LoadBalanced
  public WebClient.Builder loadBalancedWebClientBuilder() {
    return WebClient.builder().baseUrl("http://reactive-stock-service");
  }

  @Bean
  public CommandLineRunner commandLineRunner(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
    return (args -> builder.build().get().uri("/stock/get/{productId}", 25).retrieve().bodyToMono(String.class)
        .onErrorReturn("Error to get stock")
        .doOnNext(s -> System.out.println(">>>>>>>>>>>>>> Server Response: " + s))
        .delayElement(Duration.ofMillis(500))
        .repeat(3)
        .subscribe()
    );
  }

}
