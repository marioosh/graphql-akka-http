## Example of GraphQL with Scala

An example Scala [GraphQL](http://facebook.github.io/graphql/) server written with [akka-http](http://doc.akka.io/docs/akka-stream-and-http-experimental/current/scala/http/) and [sangria](https://github.com/sangria-graphql/sangria).

### Running the example

```bash
sbt ~reStart
```

SBT will automatically compile and restart the server whenever the source code changes.

After the server is started you can run queries interactively using [GraphiQL](https://github.com/graphql/graphiql) by opening [http://localhost:8080](http://localhost:8080) in a browser.

### Database Configuration

This example uses an in-memory [H2](http://www.h2database.com/html/main.html) SQL database. The schema and example data will be re-created every time server starts.

If you would like to change the database configuration or use a different database, then please update `src/main/resources/application.conf`.
