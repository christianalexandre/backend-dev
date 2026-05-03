package br.pucpr.authserver.orders

import br.pucpr.authserver.users.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "Orders")
class Order(
    @Id @GeneratedValue
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.OPEN,

    @Column(nullable = false, precision = 19, scale = 2)
    var total: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    var items: MutableList<OrderItem> = mutableListOf()
) {
    fun recalculateTotal() {
        total = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal()) }
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
