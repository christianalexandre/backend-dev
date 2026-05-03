package br.pucpr.authserver.orders

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    @Query(
        """
            select o from Order o
            where (:userId is null or o.user.id = :userId)
              and (:status is null or o.status = :status)
              and (:minTotal is null or o.total >= :minTotal)
              and (:maxTotal is null or o.total <= :maxTotal)
        """
    )
    fun search(
        @Param("userId") userId: Long?,
        @Param("status") status: OrderStatus?,
        @Param("minTotal") minTotal: BigDecimal?,
        @Param("maxTotal") maxTotal: BigDecimal?,
        sort: Sort
    ): List<Order>
}
