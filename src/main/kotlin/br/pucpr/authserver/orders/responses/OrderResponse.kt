package br.pucpr.authserver.orders.responses

import br.pucpr.authserver.orders.Order
import br.pucpr.authserver.orders.OrderStatus
import java.math.BigDecimal
import java.time.Instant

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val total: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
    val items: List<OrderItemResponse>
) {
    constructor(order: Order) : this(
        id = order.id!!,
        userId = order.user.id!!,
        status = order.status,
        total = order.total,
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
        items = order.items.map { OrderItemResponse(it) }
    )
}
