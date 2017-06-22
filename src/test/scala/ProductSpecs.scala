import Models.Product
import SchemaDef._
import org.scalatest.{AsyncWordSpec, Matchers}
import sangria.execution.Executor
import sangria.macros._
import sangria.marshalling.sprayJson._
import spray.json._

class ProductSpecs extends AsyncWordSpec with Matchers {

  val repository = ShopRepository.createDatabase()
  val resolver = SchemaDef.deferredResolver

  "A Math" should {
    "still works" in {
      (1 + 1) shouldEqual 2
    }
  }

  "A Shop" should {
    "return list of products" in {

      val query =
        graphql"""

          query MyProduct {
            product(id: 2) {
                  name
                  description

                  picture(size: 300) {
                    width, height, url
                  }
            }

            products(ids: [1,2]) {
              name
            }

            allCategories {
              name
            }
          }

      """

      val response =
        """
          |{"data":
          |  {
          |    "product":{
          |     "name":"Health Potion",
          |     "description":"+50 HP",
          |     "picture":{
          |       "width":300,
          |       "height":300,
          |       "url":"http://fakeimg.pl/300/?text=ID:%202"
          |     }
          |    },
          |    "products":[
          |     {"name":"Cheescake"},
          |     {"name":"Health Potion"}
          |    ],
          |    "allCategories":[
          |     {"name":"Food"},
          |     {"name":"Magic ingredients"},
          |     {"name":"Home interior"}
          |    ]
          |  }
          |}
        """.stripMargin.parseJson


      Executor.execute(ShopSchema, query, repository, deferredResolver = resolver) map {
        result => assert(result == response)
      }

    }

    "returns categories for provided products ids" in {

      repository.categoriesByProducts(Seq(6)) map {
        categories =>
          assert(categories.length == 2)
      }
    }

    "returns tupled of products for provided categories" in {

      repository.productsByCategories(Seq("2")) map {
        products =>
          assert(products.length == 4)
          assert(products.contains((Seq("2"), Product(6, "Candle", "", BigDecimal(13.99)))))
      }
    }

  }


}

