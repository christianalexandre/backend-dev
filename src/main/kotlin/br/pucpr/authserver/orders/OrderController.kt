package br.pucpr.authserver.orders

import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.orders.requests.AddOrderItemRequest
import br.pucpr.authserver.orders.requests.PayOrderRequest
import br.pucpr.authserver.orders.requests.UpdateOrderItemRequest
import br.pucpr.authserver.orders.responses.OrderResponse
import br.pucpr.authserver.security.UserToken
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/orders")
@SecurityRequirement(name = "jwt-auth")
class OrderController(val service: OrderService) {

    @PostMapping
    fun create(auth: Authentication): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.create(token)
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse(order))
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) all: Boolean = false,
        @RequestParam(required = false) status: OrderStatus? = null,
        @RequestParam(required = false) minTotal: BigDecimal? = null,
        @RequestParam(required = false) maxTotal: BigDecimal? = null,
        @RequestParam(required = false) sortBy: String = "createdAt",
        @RequestParam(required = false) sortDir: String = "DESC",
        auth: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val token = principal(auth)
        val direction = parseDirection(sortDir)
        val orders = service.search(token, all, status, minTotal, maxTotal, sortBy, direction)
        return ResponseEntity.ok(orders.map { OrderResponse(it) })
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, auth: Authentication): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.findById(id, token)
        return ResponseEntity.ok(OrderResponse(order))
    }

    @DeleteMapping("/{id}")
    fun cancel(@PathVariable id: Long, auth: Authentication): ResponseEntity<Void> {
        val token = principal(auth)
        service.cancel(id, token)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/items")
    fun addItem(
        @PathVariable id: Long,
        @Valid @RequestBody body: AddOrderItemRequest,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.addItem(id, body.productId!!, body.quantity!!, token)
        return ResponseEntity.ok(OrderResponse(order))
    }

    @PatchMapping("/{id}/items/{productId}")
    fun updateItem(
        @PathVariable id: Long,
        @PathVariable productId: Long,
        @Valid @RequestBody body: UpdateOrderItemRequest,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.updateItemQuantity(id, productId, body.quantity!!, token)
        return ResponseEntity.ok(OrderResponse(order))
    }

    @DeleteMapping("/{id}/items/{productId}")
    fun removeItem(
        @PathVariable id: Long,
        @PathVariable productId: Long,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.removeItem(id, productId, token)
        return ResponseEntity.ok(OrderResponse(order))
    }

    @PostMapping("/{id}/checkout")
    fun checkout(@PathVariable id: Long, auth: Authentication): ResponseEntity<OrderResponse> {
        val token = principal(auth)
        val order = service.checkout(id, token)
        return ResponseEntity.ok(OrderResponse(order))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/pay")
    fun pay(
        @PathVariable id: Long,
        @RequestBody(required = false) body: PayOrderRequest?
    ): ResponseEntity<OrderResponse> {
        val approved = body?.approved ?: true
        val order = service.pay(id, approved)
        return ResponseEntity.ok(OrderResponse(order))
    }

    private fun principal(auth: Authentication): UserToken =
        auth.principal as? UserToken ?: throw ForbiddenException()

    private fun parseDirection(sortDir: String): Sort.Direction =
        Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.DESC)
}
