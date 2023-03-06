package de.saqer.twittervideodownloadbot.twitter.oauth2.repository;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DynamoDBOAuth2CredentialsRepository implements OAuth2CredentialsRepository {
    private static final String HASH_PROP_NAME = "ID";
    private static final String HASH_PROP_VALUE = "OAuth2-Credentials";
    private static final String LIST_PROP_NAME = "OAuth2-Credentials-List";


    private final DynamoDB dynamoDBInstance;
    private final String tableName;
    private final Gson gson;

    public DynamoDBOAuth2CredentialsRepository(DynamoDB dynamoDBInstance, String tableName, Gson gson) {
        this.dynamoDBInstance = dynamoDBInstance;
        this.tableName = tableName;
        this.gson = gson;
    }

    private List<OAuth2Credentials> get() {
        Table table = dynamoDBInstance.getTable(tableName);
        Item item = table.getItem(new KeyAttribute(HASH_PROP_NAME, HASH_PROP_VALUE));
        if (item != null) {
            item.getString(LIST_PROP_NAME);
            TypeToken<Collection<OAuth2Credentials>> collectionType = new TypeToken<Collection<OAuth2Credentials>>() {
            };
            Collection<OAuth2Credentials> credentialsCollection = gson.fromJson(item.getString(LIST_PROP_NAME), collectionType);
            return new ArrayList<>(credentialsCollection);
        }

        return new ArrayList<>();
    }

    private void save(List<OAuth2Credentials> oAuth2CredentialsList) {
        Table table = dynamoDBInstance.getTable(tableName);
        Item item = new Item().withPrimaryKey(HASH_PROP_NAME, HASH_PROP_VALUE)
                .withString(LIST_PROP_NAME, gson.toJson(oAuth2CredentialsList));
        table.putItem(item);
    }

    @Override
    public OAuth2Credentials getFirstUsedCredentials() {
        List<OAuth2Credentials> credentialsList = get();
        if (credentialsList.isEmpty()) {
            throw new RuntimeException("credentials not found");
        }
        OAuth2Credentials credentials = credentialsList.get(0);
        for (OAuth2Credentials c :
                credentialsList) {
            if (c.getUsedAtTimestamp() < credentials.getUsedAtTimestamp()) {
                credentials = c;
            }
        }

        return credentials;
    }

    @Override
    public void add(OAuth2Credentials oAuth2Credentials) {
        List<OAuth2Credentials> oAuth2CredentialsList = get();
        oAuth2CredentialsList = oAuth2CredentialsList.stream().filter(c -> !Objects.equals(c.getUserId(), oAuth2Credentials.getUserId())).collect(Collectors.toList());
        oAuth2CredentialsList.add(oAuth2Credentials);
        save(oAuth2CredentialsList);
    }
}
