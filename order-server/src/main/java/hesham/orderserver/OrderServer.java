package hesham.orderserver;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@RestController
@EnableFeignClients
public class OrderServer {
    @Autowired
    private PaymentClient paymentClient;

    public static void main(String[] args) {
        SpringApplication.run(OrderServer.class, args);
    }

    @Value("${message}")
    private String message;

    @GetMapping("/message")
    public String message() {
        return message;
    }

    @GetMapping("/orders")
    public String order() {
       return paymentClient.getPayment();
    }


    //retry and circuit breaker
    @CircuitBreaker(
            name = "payment-service",
            fallbackMethod = "paymentFallback"
    )
    @TimeLimiter(
            name = "payment-service",
            fallbackMethod = "paymentTimeoutFallback"
    )
    @GetMapping("/orders/{id}")
    public CompletableFuture<String> getOrder(@PathVariable String id) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Calling Payment...");
            return paymentClient.getPayment();
        });
    }

    public CompletableFuture<String> paymentFallback(String id, Throwable e){
        return CompletableFuture.supplyAsync(() -> {
           return "Payment Service is unavailable";
        });
    }

    public CompletableFuture<String> paymentTimeoutFallback(String id, TimeoutException ex) {
        return CompletableFuture.completedFuture("Payment timed out!");
    }

    @Retry(name = "payment-service")
    @FeignClient(name = "PAYMENT-SERVICE")
    public interface PaymentClient {
        @GetMapping("/payments")
        String getPayment();
    }
    //------------

}

@Configuration
class AppConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
