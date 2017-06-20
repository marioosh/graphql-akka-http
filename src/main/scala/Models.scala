object Models {

  case class Picture(width: Int, height: Int, url: Option[String])

  trait Identifiable {
    def id: String
  }

  case class Product(id: String, name: String, description: String, price: BigDecimal) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"http://fakeimg.pl/$size/?text=ID:%20$id"))
  }

  case class Category(id: String, name: String) extends Identifiable

}
