package mttsner.cinema.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("mttsner.cinema")
@EnableJpaRepositories("mttsner.cinema")
@EnableTransactionManagement
public class DomainConfig {
}
