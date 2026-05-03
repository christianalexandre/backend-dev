package br.pucpr.authserver.orders.responses

import br.pucpr.authserver.orders.OrderItem
import java.math.BigDecimal

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productTitle: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
) {
    constructor(item: OrderItem) : this(
        id = item.id!!,
        productId = item.productId,
        productTitle = item.productTitle,
        unitPrice = item.unitPrice,
        quantity = item.quantity,
        subtotal = item.subtotal()
    )
}
