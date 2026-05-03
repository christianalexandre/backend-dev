package br.pucpr.authserver.orders

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "OrderItem")
class OrderItem(
    @Id @GeneratedValue
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    var order: Order? = null,

    @Column(nullable = false)
    var productId: Long,

    @Column(nullable = false)
    var productTitle: String,

    @Column(nullable = false, precision = 19, scale = 2)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var quantity: Int
) {
    fun subtotal(): BigDecimal = unitPrice.multiply(BigDecimal(quantity))
}
