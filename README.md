Spatial Demo Server Extension
================================

This is an unmanaged extension that extends the Neo4j API to provide endpoints that power the demo here: [spatialcypherdemo.herokuapp.com](http://spatialcypheremo.herokuapp.com).
This project uses this [Neo4j unmanaged extension template](https://github.com/dmontag/neo4j-unmanaged-extension-template) and some remnants of the template remain.
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

* Neo4j Spatial
* Google Gson

See step 3 below.

## Installation
1. Build it: 

        mvn clean package
        
   **NOTE:** use `-Dskiptests` to build without tests.

2. Copy target/unmanaged-extension-template-1.0.jar to the plugins/ directory of your Neo4j server.

3. Copy target/dependency/gson-2.2.4.jar and target/dependency/neo4j-spatial-0.13-neo4j-2.1.2.jar to the plugins/ directory of your Neo4j server.

4. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=org.neo4j.example.unmanagedextension=/

5. Start Neo4j server.

6. Query it over HTTP:

        curl http://localhost:7474/example/helloworld

