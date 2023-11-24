package hu.auxin.ibkrfacade;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

import lombok.Data;

@Data
@EnableAsync
@EnableScheduling
@EnableRedisEnhancedRepositories(basePackages = { "hu.auxin.ibkrfacade.repositories.hashes",
                "hu.auxin.ibkrfacade.models.hashes" })
@EnableRedisDocumentRepositories(basePackages = { "hu.auxin.ibkrfacade.repositories.json",
                "hu.auxin.ibkrfacade.models.json",
})
@ComponentScan(basePackages = { "hu.auxin.ibkrfacade.*" })
@SpringBootApplication(scanBasePackages = {
                "hu.auxin.ibkrfacade.*" })
// @SpringBootApplication
public class SkeletonApplication {

        @Bean
        public GroupedOpenApi httpApi() {
                String packagesToScan[] = {
                                "hu.auxin.ibkrfacade" };
                return GroupedOpenApi.builder().group("http").pathsToMatch("/**").packagesToScan(packagesToScan)
                                .build();
        }

        public static void main(String[] args) {
                SpringApplication.run(SkeletonApplication.class, args);
        }

}
