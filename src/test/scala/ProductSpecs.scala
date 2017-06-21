import org.scalatest.{Matchers, AsyncWordSpec}

import sangria.macros._
import sangria.execution.Executor
import sangria.marshalling.sprayJson._
import spray.json._
import Types._

class ProductSpecs extends AsyncWordSpec with Matchers {

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

            products {
              name
            }

            categories {
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
          |    "categories":[
          |     {"name":"Food"},
          |     {"name":"Ingredients"}
          |    ]
          |  }
          |}
        """.stripMargin.parseJson



      Executor.execute(ShopSchema, query, ShopRepository.createDatabase()) map {
        result => assert(result == response)
      }

    }
  }

}

