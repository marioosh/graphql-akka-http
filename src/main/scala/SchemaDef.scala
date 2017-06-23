import sangria.execution.deferred.{DeferredResolver, Fetcher, Relation, RelationIds}
import sangria.schema._

object SchemaDef {

  import Models._
  import sangria.macros.derive._

  //category has relation to product
  //category id's type is String
  val product = Relation[Product, (Seq[String], Product), String]("product-category", _._1, _._2)
  //product has relation to category
  //product id's type is Int
  val category = Relation[Category, (Seq[Int], Category), Int]("category-product", _._1, _._2)

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
      IncludeMethods("picture"), //by defaul macro cosinders fields only
      AddFields(
        Field("categories", ListType(CategoryType),
          complexity = constantComplexity(30),
          resolve = c => categoriesFetcher.deferRelSeq(category, c.value.id))
      )
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
      ObjectTypeDescription("The category of products"),
      AddFields(
        Field("products", ListType(ProductType),
          complexity = constantComplexity(30),
          resolve = c => productsFetcher.deferRelSeq(product, c.value.id))
      )
    )

  val productsFetcher: Fetcher[ShopRepository, Product, (Seq[String], Product), Int] = Fetcher.relCaching(
    (repo: ShopRepository, ids: Seq[Int]) => repo.products(ids),
    (repo: ShopRepository, ids: RelationIds[Product]) => repo.productsByCategories(ids(product))
  )

  val categoriesFetcher: Fetcher[ShopRepository, Category, (Seq[Int], Category), String] = Fetcher.relCaching(
    (repo: ShopRepository, ids: Seq[String]) => repo.categories(ids),
    (repo: ShopRepository, ids: RelationIds[Category]) => repo.categoriesByProducts(ids(category))
  )

  val deferredResolver = DeferredResolver.fetchers(productsFetcher, categoriesFetcher)

  val QueryType = ObjectType(
    "Query",
    fields[ShopRepository, Unit](
      Field("allProducts", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        complexity = constantComplexity(100),
        resolve = _.ctx.allProducts
      ),
      Field("product", OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Argument("id", IntType) :: Nil,
        resolve = c => productsFetcher.defer(c.arg[Int]("id"))),
      Field("products", ListType(ProductType),
        description = Some("Returns a list of products for provided IDs."),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => productsFetcher.deferSeqOpt(c.arg[List[Int]]("ids"))
      ),
      Field("category", OptionType(CategoryType),
        description = Some("Returns a category with specific `id`."),
        arguments = Argument("id", StringType) :: Nil,
        resolve = c => categoriesFetcher.deferOpt(c.arg[String]("id"))),
      Field("categories", ListType(CategoryType),
        description = Some("Returns categories by provided ids"),
        arguments = Argument("ids", ListInputType(StringType)) :: Nil,
        complexity = constantComplexity(30),
        resolve = c => categoriesFetcher.deferSeqOpt(c.arg[List[String]]("ids"))
      ),
      Field("allCategories", ListType(CategoryType),
        description = Some("Returns a list of all available categories."),
        complexity = constantComplexity(250),
        resolve = _.ctx.allCategories
      )
    )
  )

  val IdArg = Argument("id", StringType)
  val NameArg = Argument("name", StringType)

  val Mutation = ObjectType("Mutation", fields[ShopRepository, Unit](
      Field("addCategory", CategoryType,
        arguments = IdArg :: NameArg :: Nil,
        resolve = c => c.ctx.addCategory(c.arg(IdArg), c.arg(NameArg))
      )
  ))

  val ShopSchema = Schema(QueryType, Some(Mutation)) //define entry point

  def constantComplexity[Ctx](complexity: Double) =
    Some((_: Ctx, _: Args, child: Double) ⇒ child + complexity)
}
