package de.saqer.twittervideodownloadbot.repository;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.gson.Gson;
import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;
import de.saqer.twittervideodownloadbot.twitter.oauth2.repository.DynamoDBOAuth2CredentialsRepository;
import de.saqer.twittervideodownloadbot.twitter.oauth2.repository.OAuth2CredentialsRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamoDBOAuth2CredentialsRepositoryTest {

    private static final String tableName = "table";
    private static final String hashKeyName = "ID";
    private static AmazonDynamoDB client;
    private static DynamoDB dynamoDBInstance;

    @BeforeAll
    static void beforeAll() {
        client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1"))
                .build();
        dynamoDBInstance = new DynamoDB(client);
        createTable();

    }

    @AfterAll
    static void afterAll() {
        deleteTable();
    }

    @AfterEach
    void afterEach() {
        truncateTable();
    }

    private static void deleteTable() {
        Table table = dynamoDBInstance.getTable(tableName);
        table.delete();
    }

    private static void createTable() {
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(new AttributeDefinition(hashKeyName, ScalarAttributeType.S))
                .withKeySchema(
                        new KeySchemaElement(hashKeyName, KeyType.HASH)
                )
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));

        dynamoDBInstance.createTable(createTableRequest);
    }

    private void truncateTable() {
        Table table = dynamoDBInstance.getTable(tableName);
        ScanSpec spec = new ScanSpec();
        ItemCollection<ScanOutcome> items = table.scan(spec);
        for (Item item : items) {
            String hashKey = item.getString(hashKeyName);
            PrimaryKey key = new PrimaryKey(hashKeyName, hashKey);
            table.deleteItem(key);
        }
    }

    @Test
    void testSaveGet() {


        DynamoDB dynamoDBInstance = new DynamoDB(client);
        Gson gson = new Gson();

        OAuth2CredentialsRepository repository = new DynamoDBOAuth2CredentialsRepository(dynamoDBInstance, tableName, gson);

        List<OAuth2Credentials> oAuth2CredentialsList = new ArrayList<>();

        OAuth2Credentials oAuth2Credentials = new OAuth2Credentials(123, "accessToken", "refreshToken", 123, "scope");

        oAuth2CredentialsList.add(oAuth2Credentials);

        repository.save(oAuth2CredentialsList);

        List<OAuth2Credentials> result = repository.get();

        assertEquals(1, result.size());

        assertEquals(oAuth2CredentialsList.get(0), oAuth2Credentials);

    }

    @Test
    void testSaveTwiceWorks() {
        DynamoDB dynamoDBInstance = new DynamoDB(client);
        Gson gson = new Gson();

        OAuth2CredentialsRepository repository = new DynamoDBOAuth2CredentialsRepository(dynamoDBInstance, tableName, gson);

        List<OAuth2Credentials> oAuth2CredentialsList = new ArrayList<>();

        OAuth2Credentials oAuth2Credentials = new OAuth2Credentials(123, "accessToken", "refreshToken", 123, "scope");

        oAuth2CredentialsList.add(oAuth2Credentials);

        repository.save(oAuth2CredentialsList);

        repository.save(oAuth2CredentialsList);

        List<OAuth2Credentials> result = repository.get();

        assertEquals(1, result.size());

        assertEquals(oAuth2CredentialsList.get(0), oAuth2Credentials);

    }
}
