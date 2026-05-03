package br.pucpr.authserver.orders.requests

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class UpdateOrderItemRequest(
    @field:NotNull
    @field:Min(1)
    val quantity: Int?
)
