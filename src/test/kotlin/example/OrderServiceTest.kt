package example

import example.dto.OrderRequest
import example.entity.Order
import example.entity.Product
import example.enums.OrderStatus
import example.enums.PaymentStatus
import example.exception.OutOfStockException
import example.exception.PaymentFailedException
import example.repository.OrderRepository
import example.repository.PaymentRepository
import example.repository.ProductRepository
import example.service.OrderService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class OrderServiceCasesTest {

    @Autowired
    lateinit var orderService: OrderService

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    // Case 1: 재고 부족
    @Test
    fun `재고 부족이면 OutOfStockException 발생하고 주문이 저장되지 않는다`() {
        // Given: 재고 5개인 상품 생성
        val product = productRepository.save(
            Product(name = "Test Product Case1", price = 100.0, stock = 5)
        )
        // 주문 요청: 재고보다 많은 수량 (예: 10개)
        val request = OrderRequest(
            productId = product.id!!,
            quantity = 10,
            cardInfo = "ANY_CARD"
        )
        // When: 주문 생성 시도하면 재고 부족 예외 발생
        assertThrows<OutOfStockException> {
            orderService.placeOrderCase1(request)
        }
        // Then: 주문, 상품 롤백 확인
        assertTrue(orderRepository.findAll().isEmpty())
    }

    // Case 2: 결제 오류
    @Test
    fun `결제 오류 발생 시 PaymentFailedException 발생하고 주문은 롤백되지만 결제 내역은 기록된다`() {
        // Given: 재고 10개인 상품 생성
        val product = productRepository.save(
            Product(name = "Test Product Case2", price = 200.0, stock = 10)
        )
        // 주문 요청: 올바른 수량이지만, 카드 정보가 INVALID_CARD로 결제 실패 시뮬레이션
        val request = OrderRequest(
            productId = product.id!!,
            quantity = 2,
            cardInfo = "INVALID_CARD"  // PaymentGateway.validateCard에서 false 반환하도록 가정
        )
        // When: 주문 생성 시 PaymentFailedException 발생
        assertThrows<PaymentFailedException> {
            orderService.placeOrderCase2(request)
        }
        // Then: 주문, 상품 롤백 확인
        assertTrue(orderRepository.findAll().isEmpty())
        // 그리고 결제 내역은 별도 트랜잭션으로 커밋되어 있어야 함.
        // PaymentRepository.findAll()를 통해 FAILED 상태의 Payment가 있는지 확인.
        val payments = paymentRepository.findAll()
        assertFalse(payments.isEmpty())
        val failedPayment = payments.find { it.paymentStatus == PaymentStatus.FAILED }
        assertNotNull(failedPayment)
    }
}
