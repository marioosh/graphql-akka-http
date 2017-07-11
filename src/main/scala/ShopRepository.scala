import Models._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ShopRepository(db: Database) {

  import ShopRepository._

  def allProducts = db.run(Products.result)

  def products(ids: Seq[ProductId]): Future[Seq[Product]] = db.run(Products.filter(_.id inSet ids).result)

  def allCategories = db.run(Categories.result)

  def categories(ids: Seq[CategoryId]): Future[Seq[Category]] = db.run(Categories.filter(_.id inSet ids).result)

  def productsByCategories(categoriesIds: Seq[CategoryId]): Future[Seq[(Seq[CategoryId], Product)]] =
    db.run(
      Taxonometry
        .filter(_.categoryId inSet categoriesIds)
        .join(Products).on(_.productId === _.id)
        .result)
      .map { result =>
        result.groupBy(_._2.id).toVector.map {
          case (_, products) ⇒ products.map(_._1.categoryId) → products.head._2
        }
      }

  def categoriesByProducts(productsIds: Seq[CategoryId]): Future[Seq[(Seq[CategoryId], Category)]] =
    db.run(
      Taxonometry
        .filter(_.productId inSet productsIds)
        .join(Categories).on(_.categoryId === _.id)
        .result)
      .map { result =>
        result.groupBy(_._2.id).toVector.map {
          case (_, categories) ⇒ categories.map(_._1.productId) → categories.head._2
        }
      }

  def addCategory(id: CategoryId, name: String): Future[Category] = {
    val cat: Category = Category(id, name)
    db.run(Categories.insertOrUpdate(cat)).map(_ => cat)
  }

}

object ShopRepository {

  class ProductTable(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def id = column[ProductId]("PRODUCT_ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def description = column[String]("DESCRIPTION")

    def price = column[BigDecimal]("PRICE")

    def * = (id, name, description, price) <> ((Product.apply _).tupled, Product.unapply)
  }

  val Products = TableQuery[ProductTable]

  class CategoryTable(tag: Tag) extends Table[Category](tag, "CATEGORY") {
    def id = column[CategoryId]("CATEGORY_ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)
  }

  val Categories = TableQuery[CategoryTable]


  /**
    * JOIN TABLE
    */
  class TaxonomyTable(tag: Tag) extends Table[Taxonomy](tag, "PRODUCT_CATEGORY") {
    def productId = column[ProductId]("PRODUCT_ID")

    def categoryId = column[CategoryId]("CATEGORY_ID")

    //relations
    def product = foreignKey("PRODUCT_FK", productId, Products)(_.id)

    def category = foreignKey("CATEGORY_FK", categoryId, Categories)(_.id)

    def idx = index("UNIQUE_IDX", (productId, categoryId), unique = true)

    def * = (productId, categoryId) <> ((Taxonomy.apply _).tupled, Taxonomy.unapply)
  }

  val Taxonometry = TableQuery[TaxonomyTable]

  val databaseSetup = DBIO.seq(
    (Products.schema ++ Categories.schema ++ Taxonometry.schema).create,

    Products ++= Seq(
      Product(1, "Cheescake", "Tasty", BigDecimal(12.34)),
      Product(2, "Health Potion", "+50 HP", BigDecimal(98.89)),
      Product(3, "Pineapple", "The biggest one", BigDecimal(0.99)),
      Product(4, "Bull's egg", "The left one", BigDecimal(100.99)),
      Product(5, "Water", "Bottled", BigDecimal(0.25)),
      Product(6, "Candle", "", BigDecimal(13.99))
    ),
    Categories ++= Seq(
      Category(1, "Food"),
      Category(2, "Magic ingredients"),
      Category(3, "Home interior")
    ),
    Taxonometry ++= Seq(
      Taxonomy(1, 1),
      Taxonomy(2, 2),
      Taxonomy(3, 1),
      Taxonomy(4, 1),
      Taxonomy(4, 2),
      Taxonomy(5, 1),
      Taxonomy(5, 2),
      Taxonomy(6, 3),
      Taxonomy(6, 2)
    )
  )

  def createDatabase() = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(databaseSetup), 10 seconds)

    new ShopRepository(db)
  }

}
