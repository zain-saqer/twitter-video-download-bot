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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamoDBOAuth2CredentialsRepositoryTest {

    private static final String tableName = "table";
    private static final String hashKeyName = "ID";
    private static DynamoDB dynamoDBInstance;

    @BeforeAll
    static void beforeAll() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
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
    void testAddGet() {
        OAuth2Credentials c0 = new OAuth2Credentials(0, "","", 0, "", "0", "username");
        OAuth2Credentials c1 = new OAuth2Credentials(10, "","", 0, "", "1", "username");
        OAuth2Credentials c2 = new OAuth2Credentials(11, "", "", 0, "", "2", "username");
        OAuth2Credentials c3 = new OAuth2Credentials(1, "", "", 0, "", "0", "username");

        OAuth2CredentialsRepository repository = new DynamoDBOAuth2CredentialsRepository(dynamoDBInstance, tableName, new Gson());
        repository.add(c2);
        assertEquals(c2.getUsedAtTimestamp(), repository.getFirstUsedCredentials().getUsedAtTimestamp());
        repository.add(c0);
        assertEquals(c0.getUsedAtTimestamp(), repository.getFirstUsedCredentials().getUsedAtTimestamp());
        repository.add(c1);
        assertEquals(c0.getUsedAtTimestamp(), repository.getFirstUsedCredentials().getUsedAtTimestamp());
        repository.add(c3);
        assertEquals(c3.getUsedAtTimestamp(), repository.getFirstUsedCredentials().getUsedAtTimestamp());
    }
}
