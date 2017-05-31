/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student:
 * Description: Simple tests to learn MongoDB
 */

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.*;
import static com.mongodb.client.model.Filters.*;

public class UsingFilters {

    private MongoClient   mongoClient;
    private MongoDatabase soarf17;

    UsingFilters() {
        this.mongoClient = new MongoClient(Configuration.DB_SERVER, Configuration.DB_PORT);
        this.soarf17 = this.mongoClient.getDatabase(Configuration.DB_NAME);
    }

    void simpleQuery() {
        MongoCollection employees = this.soarf17.getCollection("employees");
        MongoCursor<Document> cursor = employees.find(eq("name", "Thyago")).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            System.out.println(doc.toJson());
        }
        cursor.close();
    }

    public static void main(String[] args) {
        try {
            UsingFilters usingFilters = new UsingFilters();
            usingFilters.simpleQuery();
        }
        catch (Exception ex) {
            System.out.println("Ops, something went wrong: " + ex);
        }
    }
}