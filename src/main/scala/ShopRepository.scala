import Models.{Category, Product}
import language.postfixOps
import scala.concurrent.Await
import scala.concurrent.duration._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class ShopRepository(db: Database) {

  import ShopRepository._

  def product(id: Int) = db.run(Products.filter(_.id === id).result.headOption)

  def products = db.run(Products.result)

  def category(id: Int) = db.run(Categories.filter(_.id === id).result.headOption)

  def categories = db.run(Categories.result)


  def close() = db.close()
}

object ShopRepository {

  class ProductTable(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def id = column[Int]("PRODUCT_ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def description = column[String]("DESCRIPTION")

    def price = column[BigDecimal]("PRICE")

    def * = (id, name, description, price) <> ((Product.apply _).tupled, Product.unapply)
  }

  val Products = TableQuery[ProductTable]

  class CategoryTable(tag: Tag) extends Table[Category](tag, "CATEGORY") {
    def id = column[Int]("CATEGORY_ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)
  }

  val Categories = TableQuery[CategoryTable]

  val databaseSetup = DBIO.seq(
    (Products.schema ++ Categories.schema).create,

    Products ++= Seq(
      Product(1, "Cheescake", "Tasty", BigDecimal(12.34)),
      Product(2, "Health Potion", "+50 HP", BigDecimal(98.89))
    ),
    Categories ++= Seq(
      Category(1, "Food"),
      Category(2, "Ingredients")
    )
  )

  def createDatabase() = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(databaseSetup), 10 seconds)

    new ShopRepository(db)
  }

}
