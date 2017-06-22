import sangria.execution.deferred.HasId

object Models {

  case class Picture(width: Int, height: Int, url: Option[String])

  trait Identifiable {
    def id: Int
  }

  case class Product(id: Int, name: String, description: String, price: BigDecimal) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"http://fakeimg.pl/$size/?text=ID:%20$id"))
  }

  object Product {
    implicit val hasId = HasId[Product, Int](_.id)
  }

  case class Category(id: String, name: String)

  object Category {
    implicit val hasId = HasId[Category, String](_.id)
  }

  case class Taxonomy(productId: Int, categoryId: String)

}
