package net.mineskyevents.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.mineskyevents.MineSkyEvents;
import org.bson.Document;

public class MongoDBManager {
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> eventos;

    public void connect(String uri, String dbName) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();

        try {
            client = MongoClients.create(settings);
            db = client.getDatabase(dbName);
            eventos = db.getCollection("eventos");
            MineSkyEvents.l.info("| Conectado ao MongoDB!");
        } catch (Exception e) {
            MineSkyEvents.l.severe("| Falha ao conectar ao MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getEventos() {
        return eventos;
    }

    public void close() {
        if (client != null) {
            client.close();
            MineSkyEvents.l.info("| Desconectado do MongoDB.");
        }
    }
}
