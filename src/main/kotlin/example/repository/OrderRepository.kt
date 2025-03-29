package example.repository

import example.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface OrderRepository: JpaRepository<Order, Long> {
}
