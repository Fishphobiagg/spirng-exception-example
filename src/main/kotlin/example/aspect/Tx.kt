package example.aspect

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Component
class Tx(
    _txAdvice: TxAdvice,
) {

    init {
        txAdvice = _txAdvice
    }

    companion object {
        private lateinit var txAdvice: TxAdvice

        fun <T> run(function: () -> T): T {
            return txAdvice.run(function)
        }

        fun <T> readOnly(function: () -> T): T {
            return txAdvice.readOnly(function)
        }

        fun <T> requiresNew(function: () -> T): T {
            return txAdvice.requiresNew(function)
        }
    }

    @Component
    class TxAdvice {

        @Transactional
        fun <T> run(function: () -> T): T {
            return function()
        }

        @Transactional(readOnly = true)
        fun <T> readOnly(function: () -> T): T {
            return function()
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        fun <T> requiresNew(function: () -> T): T {
            return function()
        }
    }
}
