package br.pucpr.authserver.external.dummyjson

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class DummyJsonConfig {
    @Bean
    fun dummyJsonRestClient(
        @Value("\${dummyjson.base-url}") baseUrl: String
    ): RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()
}
