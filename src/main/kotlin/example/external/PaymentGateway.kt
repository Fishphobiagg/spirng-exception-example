package example.external

interface PaymentGateway {
    fun validateCard(cardInfo: String): Boolean
    fun checkBalance(cardInfo: String, amount: Double): Boolean
    fun requestPayment(cardInfo: String, amount: Double)
}
