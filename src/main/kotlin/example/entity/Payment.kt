package example.entity

import example.enums.PaymentStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
data class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    @Column(nullable = false)
    var amount: Double,
    @Column(nullable = false)
    var paymentDate: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentStatus: PaymentStatus,
)
