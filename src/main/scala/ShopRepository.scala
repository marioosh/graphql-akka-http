import Models.Product

class ShopRepository {
  private val Products = List(
    Product("1", "Cheescake", "Tasty", BigDecimal(12.34)),
    Product("2", "Health Potion", "+50 HP", BigDecimal(98.89))
  )

  def product(id: String): Option[Product] =
    Products find(_.id == id)

  def products: List[Product] = Products

}
