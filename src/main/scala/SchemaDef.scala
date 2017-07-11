import sangria.execution.deferred.{DeferredResolver, Fetcher}
import sangria.schema._

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

  implicit val ProductType: ObjectType[Unit, Product] =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture") //by defaul macro cosinders fields only
    )

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
      Interfaces(IdentifiableType),
      ObjectTypeDescription("The category of products")
    )

  val productFetcher = Fetcher(
    (repo: ShopRepository, ids: Seq[ProductId]) => repo.products(ids)
  )

  val categoriesFetcher = Fetcher(
    (repo: ShopRepository, ids: Seq[CategoryId]) => repo.categories(ids)
  )

  val deferredResolver = DeferredResolver.fetchers(productFetcher, categoriesFetcher)

  val QueryType = ObjectType(
    "Query",
    fields[ShopRepository, Unit](
      Field("allProducts", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.allProducts
      ),
      Field("product", OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Argument("id", IntType) :: Nil,
        resolve = c => productFetcher.defer(c.arg[ProductId]("id"))),
      Field("products", ListType(ProductType),
        description = Some("Returns a list of products for provided IDs."),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => productFetcher.deferSeqOpt(c.arg[List[ProductId]]("ids"))
      ),
      Field("category", OptionType(CategoryType),
        description = Some("Returns a category with specific `id`."),
        arguments = Argument("id", IntType) :: Nil,
        resolve = c => categoriesFetcher.deferOpt(c.arg[CategoryId]("id"))),
      Field("categories", ListType(CategoryType),
        description = Some("Returns categories by provided ids"),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => categoriesFetcher.deferSeqOpt(c.arg[List[CategoryId]]("ids"))
      ),
      Field("allCategories", ListType(CategoryType),
        description = Some("Returns a list of all available categories."),
        resolve = _.ctx.allCategories
      )
    )
  )

  val ShopSchema = Schema(QueryType) //define entry point
}
