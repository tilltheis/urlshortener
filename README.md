Practice project to refresh knowledge of
- http4s
- ZIO
- ZIO test
- ZIO mock

This runs on Java 17.

Run it with `sbt run`.
```shell
$ curl -X POST -d http://example.org http://localhost:8080
http://localhost:8080/881775f0-01bb-4986-845f-2817909ae725

$ curl http://localhost:8080/881775f0-01bb-4986-845f-2817909ae725
http://example.org
```

Test it with `sbt test`.
