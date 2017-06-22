import SchemaDef._
import org.scalatest.{AsyncWordSpec, Matchers}
import sangria.execution.Executor
import sangria.macros._
import sangria.marshalling.sprayJson._
import spray.json._

class ProductSpecs extends AsyncWordSpec with Matchers {

  val repository = ShopRepository.createDatabase()

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
          |    ]
          |  }
          |}
        """.stripMargin.parseJson


      Executor.execute(ShopSchema, query, repository) map {
        result => assert(result == response)
      }

    }

  }


}

