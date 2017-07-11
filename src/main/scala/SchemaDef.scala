import sangria.execution.deferred.{DeferredResolver, Fetcher, Relation, RelationIds}
import sangria.schema._

object SchemaDef {

  import Models._
  import sangria.macros.derive._

  //category has relation to product
  val product = Relation[Product, (Seq[CategoryId], Product), CategoryId]("product-category", _._1, _._2)
  //product has relation to category
  val category = Relation[Category, (Seq[ProductId], Category), ProductId]("category-product", _._1, _._2)

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

  val productsFetcher: Fetcher[ShopRepository, Product, (Seq[ProductId], Product), ProductId] = Fetcher.relCaching(
    (repo: ShopRepository, ids: Seq[ProductId]) => repo.products(ids),
    (repo: ShopRepository, ids: RelationIds[Product]) => repo.productsByCategories(ids(product))
  )

  val categoriesFetcher: Fetcher[ShopRepository, Category, (Seq[CategoryId], Category), CategoryId] = Fetcher.relCaching(
    (repo: ShopRepository, ids: Seq[CategoryId]) => repo.categories(ids),
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
        resolve = c => productsFetcher.defer(c.arg[ProductId]("id"))),
      Field("products", ListType(ProductType),
        description = Some("Returns a list of products for provided IDs."),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => productsFetcher.deferSeqOpt(c.arg[List[ProductId]]("ids"))
      ),
      Field("category", OptionType(CategoryType),
        description = Some("Returns a category with specific `id`."),
        arguments = Argument("id", IntType) :: Nil,
        resolve = c => categoriesFetcher.deferOpt(c.arg[CategoryId]("id"))),
      Field("categories", ListType(CategoryType),
        description = Some("Returns categories by provided ids"),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        complexity = constantComplexity(30),
        resolve = c => categoriesFetcher.deferSeqOpt(c.arg[List[CategoryId]]("ids"))
      ),
      Field("allCategories", ListType(CategoryType),
        description = Some("Returns a list of all available categories."),
        complexity = constantComplexity(250),
        resolve = _.ctx.allCategories
      )
    )
  )

  val IdArg = Argument("id", IntType)
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
