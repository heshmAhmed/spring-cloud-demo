package hesham.pamentservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PaymentService {

	public static void main(String[] args) {
		SpringApplication.run(PaymentService.class, args);
	}

	@GetMapping("/payments")
	public String payment(@Value("${server.port}") String port)  throws InterruptedException {
		Thread.sleep(10000);
		return "Payment Service from port " + port;
	}
}
