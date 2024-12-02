package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;

import java.io.IOException;

public class MongoLoginDataSource implements LoginDataSource {
    private static String connectionString = "mongodb://localhost:27017";

    @Override
    public Result<LoggedInUser> login(String username, String password) {
        /*
        try (MongoClient client = MongoClients.create(connectionString)) {
            MongoDatabase database = client.getDatabase("michelin");
            MongoCollection<Document> collection = database.getCollection("users");

            Document document = collection.find(eq("username", username)).first();
            if (document == null) {
                return new Result.Error(new IllegalArgumentException("Username not found"));
            } else {
                if (document.getString("password").equals(password)) {
                    return new Result.Success<>(
                            new LoggedInUser(
                                    java.util.UUID.randomUUID().toString(), "Jane Doe"
                            ));
                }
            }
        }
        */

        return new Result.Error(new IOException("Yeah idk what went wrong"));
    }

    @Override
    public void logout() {
        // TODO haha
    }
}
