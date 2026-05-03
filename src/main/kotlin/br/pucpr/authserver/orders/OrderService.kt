package br.pucpr.authserver.orders

import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.external.dummyjson.DummyJsonClient
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class OrderService(
    val repository: OrderRepository,
    val userRepository: UserRepository,
    val dummyJsonClient: DummyJsonClient
) {
    fun create(requester: UserToken): Order {
        val user = userRepository.findByIdOrNull(requester.id)
            ?: throw NotFoundException("User ${requester.id} not found")
        val order = Order(user = user)
        val saved = repository.save(order)
        log.info("Order ${saved.id} created for user ${user.id}")
        return saved
    }

    fun findById(id: Long, requester: UserToken): Order {
        val order = repository.findByIdOrNull(id) ?: throw NotFoundException(id)
        ensureCanAccess(order, requester)
        return order
    }

    fun search(
        requester: UserToken,
        all: Boolean,
        status: OrderStatus?,
        minTotal: BigDecimal?,
        maxTotal: BigDecimal?,
        sortBy: String,
        sortDir: Sort.Direction
    ): List<Order> {
        if (all && !requester.isAdmin) {
            throw ForbiddenException("Only admins can list all orders")
        }
        val allowedSortFields = setOf("createdAt", "updatedAt", "total", "status")
        if (sortBy !in allowedSortFields) {
            throw BadRequestException("Invalid sortBy. Allowed: $allowedSortFields")
        }
        val userId = if (all) null else requester.id
        return repository.search(userId, status, minTotal, maxTotal, Sort.by(sortDir, sortBy))
    }

    fun addItem(orderId: Long, productId: Long, quantity: Int, requester: UserToken): Order {
        val order = findById(orderId, requester)
        ensureOpen(order)

        val product = dummyJsonClient.fetchProduct(productId)
        val existing = order.items.find { it.productId == productId }
        val newQuantity = (existing?.quantity ?: 0) + quantity
        ensureStock(product.stock, newQuantity, productId)

        if (existing != null) {
            existing.quantity = newQuantity
            existing.unitPrice = product.price
            existing.productTitle = product.title
            log.info("Order $orderId: incremented product $productId to $newQuantity")
        } else {
            val item = OrderItem(
                order = order,
                productId = product.id,
                productTitle = product.title,
                unitPrice = product.price,
                quantity = quantity
            )
            order.items.add(item)
            log.info("Order $orderId: added product $productId qty=$quantity")
        }
        order.recalculateTotal()
        order.updatedAt = Instant.now()
        return repository.save(order)
    }

    fun updateItemQuantity(orderId: Long, productId: Long, quantity: Int, requester: UserToken): Order {
        val order = findById(orderId, requester)
        ensureOpen(order)
        val item = order.items.find { it.productId == productId }
            ?: throw NotFoundException("Product $productId not in order $orderId")

        val product = dummyJsonClient.fetchProduct(productId)
        ensureStock(product.stock, quantity, productId)

        item.quantity = quantity
        item.unitPrice = product.price
        order.recalculateTotal()
        order.updatedAt = Instant.now()
        log.info("Order $orderId: updated product $productId to qty=$quantity")
        return repository.save(order)
    }

    fun removeItem(orderId: Long, productId: Long, requester: UserToken): Order {
        val order = findById(orderId, requester)
        ensureOpen(order)
        val removed = order.items.removeIf { it.productId == productId }
        if (!removed) {
            throw NotFoundException("Product $productId not in order $orderId")
        }
        order.recalculateTotal()
        order.updatedAt = Instant.now()
        log.info("Order $orderId: removed product $productId")
        return repository.save(order)
    }

    fun checkout(orderId: Long, requester: UserToken): Order {
        val order = findById(orderId, requester)
        ensureOpen(order)
        if (order.items.isEmpty()) {
            throw BadRequestException("Cannot checkout an empty order")
        }
        order.items.forEach { item ->
            val product = dummyJsonClient.fetchProduct(item.productId)
            ensureStock(product.stock, item.quantity, item.productId)
            item.unitPrice = product.price
            item.productTitle = product.title
        }
        order.recalculateTotal()
        order.status = OrderStatus.PENDING_PAYMENT
        order.updatedAt = Instant.now()
        log.info("Order $orderId: checkout completed, total=${order.total}")
        return repository.save(order)
    }

    fun pay(orderId: Long, approved: Boolean): Order {
        val order = repository.findByIdOrNull(orderId) ?: throw NotFoundException(orderId)
        if (order.status != OrderStatus.PENDING_PAYMENT) {
            throw BadRequestException("Order $orderId is not awaiting payment (status=${order.status})")
        }
        order.status = if (approved) OrderStatus.PAID else OrderStatus.PAYMENT_FAILED
        order.updatedAt = Instant.now()
        log.info("Order $orderId: payment ${if (approved) "approved" else "failed"}")
        return repository.save(order)
    }

    fun cancel(orderId: Long, requester: UserToken) {
        val order = findById(orderId, requester)
        if (order.status != OrderStatus.OPEN) {
            throw BadRequestException("Cannot cancel order in status ${order.status}")
        }
        order.status = OrderStatus.CANCELLED
        order.updatedAt = Instant.now()
        repository.save(order)
        log.info("Order $orderId cancelled by user ${requester.id}")
    }

    private fun ensureCanAccess(order: Order, requester: UserToken) {
        if (order.user.id != requester.id && !requester.isAdmin) {
            throw ForbiddenException("Access to order ${order.id} not allowed")
        }
    }

    private fun ensureOpen(order: Order) {
        if (order.status != OrderStatus.OPEN) {
            throw BadRequestException("Order ${order.id} is not editable (status=${order.status})")
        }
    }

    private fun ensureStock(stock: Int, requested: Int, productId: Long) {
        if (requested > stock) {
            throw BadRequestException("Insufficient stock for product $productId (available=$stock, requested=$requested)")
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(OrderService::class.java)
    }
}
