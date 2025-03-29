package example.entity

import jakarta.persistence.*

@Entity
@Table(name = "product")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    val price: Double,
    var stock: Int,
)
