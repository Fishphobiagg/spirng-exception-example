package example.exception

class InvalidPaymentException(message: String) : RuntimeException(message)
class OutOfStockException(message: String) : RuntimeException(message)
class PaymentFailedException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
