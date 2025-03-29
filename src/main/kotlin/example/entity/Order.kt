package example.entity

import example.enums.OrderStatus
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,
    @Column(nullable = false)
    val orderDate: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var totalPrice: Double = 0.0,
    // 단순화를 위해 단일 상품 ID만 기록 (여러 상품일 경우 OrderItem을 사용)
    @Column(nullable = false)
    var productId: Long
)
