package br.pucpr.authserver.orders

enum class OrderStatus {
    OPEN,
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
