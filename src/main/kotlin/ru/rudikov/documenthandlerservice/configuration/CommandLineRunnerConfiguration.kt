package ru.rudikov.documenthandlerservice.configuration

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.rudikov.documenthandlerservice.port.primary.StoragePort

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class CommandLineRunnerConfiguration {

    @Bean
    fun init(storageService: StoragePort): CommandLineRunner? {
        return CommandLineRunner {
            storageService.init()
        }
    }
}
