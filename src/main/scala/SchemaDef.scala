import sangria.execution.deferred._
import sangria.schema.{Argument, Field, IntType, InterfaceType, ListInputType, ListType, ObjectType, OptionType, Schema, StringType, fields}

object SchemaDef {

  import Models._
  import sangria.macros.derive._

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](
      Field("id", IntType, resolve = _.value.id)
    )
  )

  /**
    * PRODUCT
    */

//  val pcRelation = Relation[Category, (Seq[Int], Category), Int]("productCategory", _._1, _._2)

//

  implicit val ProductType: ObjectType[Unit, Product] =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture"), //by defaul macro cosinders fields only
//      AddFields(
//        Field("categories",
//          ListType(CategoryType),
//          description = Some("Categories assigned to the prodct"),
//          resolve = c => categoriesFetcher.deferRelSeq(pcRelation, c.value.id)
//        )
//      )
    )

  /**
    * PICTURE
    */
  implicit val PictureType: ObjectType[Unit, Picture] =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("The product picture"),
      DocumentField("url", "Picture CDN URL")
    )

  /**
    * Category
    */

  implicit val CategoryType: ObjectType[Unit, Category] =
    deriveObjectType[Unit, Category](
      ObjectTypeDescription("The category of products")
    )

  val Id = Argument("id", IntType)
  val Ids = Argument("ids", ListInputType(IntType))

  implicit val productHasId = HasId[Product, Int](_.id)
  implicit val categoryHasId = HasId[Category, Int](_.id)

  val productsFetcher = Fetcher(
    (repo: ShopRepository, ids: Seq[Int]) => repo.products(ids)
  )
  val categoriesFetcher = Fetcher(
    (repo: ShopRepository, ids: Seq[Int]) => repo.categories(ids)
  )

//  val pcRelation = Relation[Product, (Seq[Int], Category), Int]("productCategory", _._1, _._2)
//
//  val categoriesFetcher = Fetcher.relCaching(
//    (repo: ShopRepository, ids: Seq[Int]) => repo.categories(ids),
//    (repo: ShopRepository, ids: RelationIds[Product]) => repo.findProductsCategories(ids(pcRelation))
//  )

  lazy val deferredResolver = DeferredResolver.fetchers(productsFetcher, categoriesFetcher)

  val QueryType = ObjectType(
    "Query",
    fields[ShopRepository, Unit](
      Field("product", OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Id :: Nil,
        resolve = c => productsFetcher.defer(c.arg[Int]("id"))),

      Field("allProducts", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.allProducts
      ),
      Field("products", ListType(ProductType),
        description = Some("Returns a list of products for provided IDs."),
        arguments = Ids :: Nil,
        resolve = c => productsFetcher.deferSeqOpt(c.arg[List[Int]]("ids"))
      ),
      Field("category", OptionType(CategoryType),
        description = Some("Returns a category with specific `id`."),
        arguments = Id :: Nil,
        resolve = c => categoriesFetcher.deferOpt(c.arg[Int]("id"))),
      Field("categories",ListType(CategoryType),
        description = Some("Returns categories by provided ids"),
        arguments = Ids :: Nil,
        resolve = c => categoriesFetcher.deferSeqOpt(c.arg[List[Int]]("ids"))
      ),
      Field("allCategories", ListType(CategoryType),
        description = Some("Returns a list of all available categories."),
        resolve = _.ctx.allCategories
      )
    )
  )

  val ShopSchema = Schema(QueryType) //define entry point
}
