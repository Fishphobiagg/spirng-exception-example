package example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringExceptionHandleApplication

fun main(args: Array<String>) {
    runApplication<SpringExceptionHandleApplication>(*args)
}
