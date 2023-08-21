package lowestcoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LowestPriceCoinApplication {

    public static void main(String[] args) {
        SpringApplication.run(LowestPriceCoinApplication.class, args);
    }

}
