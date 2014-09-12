Spatial Demo Server Extension
================================

This is an unmanaged extension that extends the Neo4j API to provide endpoints that power the demo here: [http://spatialcypherdemo.herokuapp.com](spatialcypheremo.herokuapp.com).
The additional endpoints are:

`GET .../scdemo/intersects?polygon=WKTPOLYGON&category=xxx`
Where WKTPOLYGON is a polygon string in WKT format and xxx is a business categoy (ie. Restaurants)

`POST .../scdemo/node`
Body:

~~~json
{
    "business_id": "some_id",
    "name": "business name",
    "address": "address string",
    "lat": "latitude",
    "lon": "longitude"
}
~~~

## Dependencies


1. Build it: 

        mvn clean package

2. Copy target/unmanaged-extension-template-1.0.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=org.neo4j.example.unmanagedextension=/example

4. Start Neo4j server.

5. Query it over HTTP:

        curl http://localhost:7474/example/helloworld

