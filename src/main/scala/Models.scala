

object Models {

  type ProductId = Int
  type CategoryId = Int

  case class Picture(width: Int, height: Int, url: Option[String])

  trait Identifiable {
    def id: Int
  }

  case class Product(id: ProductId, name: String, description: String, price: BigDecimal) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"http://fakeimg.pl/$size/?text=ID:%20$id"))
  }

  case class Category(id: CategoryId, name: String) extends Identifiable

  case class Taxonomy(productId: ProductId, categoryId: CategoryId)

}
