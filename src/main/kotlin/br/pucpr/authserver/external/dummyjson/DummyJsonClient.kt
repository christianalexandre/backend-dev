package br.pucpr.authserver.external.dummyjson

import br.pucpr.authserver.exceptions.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@Component
class DummyJsonClient(val restClient: RestClient) {
    fun fetchProduct(id: Long): DummyJsonProductDto {
        log.debug("Fetching product $id from dummyjson")
        return try {
            restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(DummyJsonProductDto::class.java)
                ?: throw BadRequestException("Product $id not found")
        } catch (ex: HttpClientErrorException.NotFound) {
            throw BadRequestException("Product $id not found", ex)
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(DummyJsonClient::class.java)
    }
}
