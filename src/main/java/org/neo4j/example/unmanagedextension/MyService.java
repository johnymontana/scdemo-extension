package org.neo4j.example.unmanagedextension;

import com.google.gson.Gson;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.filter.SearchIntersect;
import org.neo4j.gis.spatial.rtree.SpatialIndexReader;
import org.neo4j.gis.spatial.rtree.filter.SearchResults;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/scdemo")
public class MyService {

    enum Labels implements Label {
        Business,
        Person
    }

    enum RelTypes implements RelationshipType {
        KNOWS,
        IS_IN
    }

    @GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World!";
    }

    @GET
    @Path("/friendsCypher/{name}")
    public Response getFriendsCypher(@PathParam("name") String name, @Context CypherExecutor cypherExecutor) throws IOException {
        ExecutionEngine executionEngine = cypherExecutor.getExecutionEngine();
        ExecutionResult result = executionEngine.execute("MATCH (p:Person)-[:KNOWS]-(friend) WHERE p.name = {n} RETURN friend.name",
                Collections.<String, Object>singletonMap("n", name));
        List<String> friendNames = new ArrayList<String>();
        for (Map<String, Object> item : result) {
            friendNames.add((String) item.get("friend.name"));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
    }

    @POST
    @Path("/node")
    public Response addNode(String nodeParamsJson, @Context GraphDatabaseService db) {
        Node businessNode;

        SpatialDatabaseService spatialDB = new SpatialDatabaseService(db);

        Gson gson = new Gson();
        BusinessNode business = gson.fromJson(nodeParamsJson, BusinessNode.class);

        try ( Transaction tx = db.beginTx()) {

            businessNode = db.createNode();
            businessNode.addLabel(Labels.Business);
            businessNode.setProperty("business_id", business.getBusiness_id());
            businessNode.setProperty("name", business.getName());
            businessNode.setProperty("address", business.getAddresss());
            businessNode.setProperty("lat", business.getLat());
            businessNode.setProperty("lon", business.getLon());
            tx.success();
        }

        try (Transaction tx = db.beginTx()) {

            Layer businessLayer = spatialDB.getOrCreatePointLayer("business", "lat", "lon");

            businessLayer.add(businessNode);
            tx.success();
        }


        return Response.ok().build();

    }

    @GET
    @Path("/intersects/")
    public Response getBusinessesInPolygon(@QueryParam("polygon") String polygon, @QueryParam("category") String category, @Context GraphDatabaseService db) throws IOException, ParseException{
        WKTReader wktreader = new WKTReader();

        ArrayList<Object> resultsArray = new ArrayList();

        SpatialDatabaseService spatialDB = new SpatialDatabaseService(db);
        Layer businessLayer = spatialDB.getOrCreatePointLayer("business", "lat", "lon");
        SpatialIndexReader spatialIndex = businessLayer.getIndex();

        SearchIntersect searchQuery = new SearchIntersect(businessLayer, wktreader.read(polygon));


        try (Transaction tx = db.beginTx()) {
            SearchResults results = spatialIndex.searchIndex(searchQuery);


            for (Node business : results) {
                for (Relationship catRel : business.getRelationships(RelTypes.IS_IN, Direction.BOTH)) {
                    Node categoryNode = catRel.getOtherNode(business);
                    if (categoryNode.getProperty("name").equals(category)) {
                        HashMap<String, Object> geojson = new HashMap<>();
                        geojson.put("lat", business.getProperty("lat"));
                        geojson.put("lon", business.getProperty("lon"));
                        geojson.put("name", business.getProperty("name"));
                        geojson.put("address", business.getProperty("address"));
                        resultsArray.add(geojson);
                    }
                }

            }

            tx.success();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(resultsArray)).build();
    }

    @GET
    @Path("/friendsJava/{name}")
    public Response getFriendsJava(@PathParam("name") String name, @Context GraphDatabaseService db) throws IOException {

        List<String> friendNames = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node person = IteratorUtil.single(db.findNodesByLabelAndProperty(Labels.Person, "name", name));

            for (Relationship knowsRel : person.getRelationships(RelTypes.KNOWS, Direction.BOTH)) {
                Node friend = knowsRel.getOtherNode(person);
                friendNames.add((String) friend.getProperty("name"));
            }
            tx.success();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(friendNames)).build();
    }
}
