import org.scalatest.{Matchers, AsyncWordSpec}

import sangria.macros._
import sangria.execution.Executor
import sangria.marshalling.sprayJson._
import spray.json._
import SchemaDef._

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


      Executor.execute(ShopSchema, query, repository,deferredResolver = resolver) map {
        result => assert(result == response)
      }

    }

    "returns categories for provided products ids" in {

      repository.findProductsCategories(Seq(6)) map {
        categories =>
          println(categories)
          assert(categories.length == 2)
      }
    }
  }


}

