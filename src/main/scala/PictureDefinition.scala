import sangria.schema._

/**
  * File needed for presentation reasons only
  */

object PictureDefinition {

  case class Picture(width: Int, height: Int, url: Option[String])

  val PictureType = ObjectType(
    "Picture",
    "The product picture",
    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType),
        description = Some("Picture CDN Url"),
        resolve = _.value.url)
    )
  )

  object ForLazyDevelopers {
    import sangria.macros.derive._

    val PictureType: ObjectType[Unit, Picture] =
      deriveObjectType[Unit, Picture](
        ObjectTypeDescription("The product picture"),
        DocumentField("url", "Picture CDN URL")
      )

  }

}
