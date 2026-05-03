package br.pucpr.authserver.external.dummyjson

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class DummyJsonProductDto(
    val id: Long,
    val title: String,
    val price: BigDecimal,
    val stock: Int
)
