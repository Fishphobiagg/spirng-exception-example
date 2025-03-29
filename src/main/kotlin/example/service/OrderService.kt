package example.service

import example.aspect.Tx
import example.dto.OrderRequest
import example.entity.Order
import example.entity.Payment
import example.entity.Product
import example.enums.OrderStatus
import example.enums.PaymentStatus
import example.exception.InvalidPaymentException
import example.exception.OutOfStockException
import example.exception.PaymentFailedException
import example.external.PaymentGateway
import example.repository.OrderRepository
import example.repository.PaymentRepository
import example.repository.ProductRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService (
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway
){
    fun placeOrderCase1(request: OrderRequest): Order = Tx.run {
        // 1. 상품 조회
        val product = productRepository.findById(request.productId)
            .orElseThrow { IllegalArgumentException("Product not found") }

        // 2. 상품 재고 확인
        if (product.stock < request.quantity) {
            // 3. 재고 부족 예외 발생
            throw OutOfStockException("Not enough stock for product ${product.id}")
        }

        // 재고 충분하면 재고 차감
        product.stock -= request.quantity
        productRepository.save(product)

        // 주문 생성 (기본 정보만 저장)
        val order = Order(
            status = example.enums.OrderStatus.PENDING,
            orderDate = LocalDateTime.now(),
            totalPrice = product.price * request.quantity,
            productId = product.id!!
        )
        orderRepository.save(order)
    }

    fun placeOrderCase2(request: OrderRequest): Order = Tx.run {
        // 상품 조회 및 재고 확인 (Product 엔티티 사용)
        val product: Product = productRepository.findById(request.productId)
            .orElseThrow { IllegalArgumentException("Product not found") }
        if (product.stock < request.quantity) {
            throw OutOfStockException("Not enough stock for product ${product.id}")
        }
        product.stock -= request.quantity
        productRepository.save(product)

        // 주문 생성 (Order 엔티티 사용)
        val order = Order(
            status = OrderStatus.PENDING,
            orderDate = LocalDateTime.now(),
            totalPrice = product.price * request.quantity,
            productId = product.id!!
        )
        val savedOrder = orderRepository.save(order)

        // 결제 내역 기록
        Tx.requiresNew {
            paymentRepository.save(
                Payment(
                    order = savedOrder,
                    amount = savedOrder.totalPrice,
                    paymentStatus = PaymentStatus.PENDING,
                    paymentDate = LocalDateTime.now()
                )
            )
        }

        try {
            // 결제 유효성 검사
            if (!paymentGateway.validateCard(request.cardInfo)) {
                throw InvalidPaymentException("Invalid card information")
            }
            if (!paymentGateway.checkBalance(request.cardInfo, savedOrder.totalPrice)) {
                throw InvalidPaymentException("Insufficient balance")
            }
            // 실제 결제 요청 (실패 시 PaymentGateway.requestPayment에서 예외 발생)
            paymentGateway.requestPayment(request.cardInfo, savedOrder.totalPrice)
            // 결제 성공 시, 별도 트랜잭션에서 Payment 상태 업데이트
            Tx.requiresNew {
                val payment = paymentRepository.findByOrderId(savedOrder.id!!)
                payment?.let {
                    it.paymentStatus = PaymentStatus.COMPLETED
                    paymentRepository.save(it)
                }
            }
        } catch (e: Exception) {
            // 결제 실패 시 Payment 상태 FAILED로 업데이트
            Tx.requiresNew {
                val payment = paymentRepository.findByOrderId(savedOrder.id!!)
                payment?.let {
                    it.paymentStatus = PaymentStatus.FAILED
                    paymentRepository.save(it)
                }
            }
            throw PaymentFailedException("Payment failed: ${e.message}", e)
        }

        // 결제 성공 시 주문 상태 업데이트
        savedOrder.status = OrderStatus.COMPLETED
        orderRepository.save(savedOrder)
        savedOrder
    }
}
