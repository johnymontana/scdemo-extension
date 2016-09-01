package org.neo4j.example.unmanagedextension;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.codehaus.jackson.map.ObjectMapper;

import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.filter.SearchIntersect;
import org.neo4j.gis.spatial.rtree.SpatialIndexReader;
import org.neo4j.gis.spatial.rtree.filter.SearchResults;
import org.neo4j.graphdb.*;

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
        IS_IN
    }

    @GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World!";
    }


    @GET
    @Path("/intersects/")
    public Response getBusinessesInPolygon(@QueryParam("polygon") String polygon, @QueryParam("category") String category, @Context GraphDatabaseService db) throws IOException, ParseException{
        WKTReader wktreader = new WKTReader();

        ArrayList<Object> resultsArray = new ArrayList();

        SpatialDatabaseService spatialDB = new SpatialDatabaseService(db);

        Layer businessLayer = spatialDB.getLayer("scdemo");
        SpatialIndexReader spatialIndex = businessLayer.getIndex();

        SearchIntersect searchQuery = new SearchIntersect(businessLayer, wktreader.read(polygon));


        try (Transaction tx = db.beginTx()) {
            SearchResults results = spatialIndex.searchIndex(searchQuery);


            for (Node business : results) {
                System.out.println(business.getProperty("name"));
                for (Relationship catRel : business.getRelationships(RelTypes.IS_IN, Direction.BOTH)) {
                    Node categoryNode = catRel.getOtherNode(business);
                    if (categoryNode.getProperty("name").equals(category)) {
                        HashMap<String, Object> geojson = new HashMap<>();
                        geojson.put("lat", business.getProperty("latitude"));
                        geojson.put("lon", business.getProperty("longitude"));
                        geojson.put("name", business.getProperty("name"));
                        resultsArray.add(geojson);
                    }
                }

            }

            tx.success();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Response.ok().entity(objectMapper.writeValueAsString(resultsArray)).build();
    }

}
