package example.dto

import example.entity.Product

/**
 * 주문 요청 DTO.
 * - productId: 주문할 상품의 ID
 * - quantity: 주문할 수량
 * - cardInfo: 결제에 사용할 카드 정보 (형식은 실제 서비스에 맞게 정의)
 */
data class OrderRequest(
    val productId: Long,
    val quantity: Int,
    val cardInfo: String
)
