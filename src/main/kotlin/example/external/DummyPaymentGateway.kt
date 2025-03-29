package example.external

import org.springframework.stereotype.Component


@Component
class DummyPaymentGateway : PaymentGateway{
    var shouldFailPayment: Boolean = false
    var shouldReturnInvalid: Boolean = false

    override fun validateCard(cardInfo: String): Boolean {
        // 카드 정보가 공백이 아니고, 플래그가 false여야 유효함
        if (cardInfo == "INVALID_CARD") throw PaymentApiException("invalid card")
        return !shouldReturnInvalid && cardInfo.isNotBlank()
    }

    override fun checkBalance(cardInfo: String, amount: Double): Boolean {
        // 간단한 금액 체크: 1000 이하이면 잔액 충분으로 가정
        return !shouldReturnInvalid && amount <= 1000.0
    }

    override fun requestPayment(cardInfo: String, amount: Double) {
        if (shouldFailPayment) {
            throw PaymentApiException("Payment API failure simulated")
        }
        // 정상 결제 성공 (아무 동작 없음)
    }
}

class PaymentApiException(message: String) : RuntimeException(message)
