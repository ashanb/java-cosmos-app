package com.mslearn;

import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.IncludedPath;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.collect.Lists;

/**
 * Hello world!
 *
 */
public class App 
{
    private static String endpointUri = "https://ashanbcosomos5.documents.azure.com:443/";
    private static String primaryKey = "NqjiAjWHbM2s9kYOB6EIqfUDkcDMky13oPaO1IeFEi3AXT3ytKrpVftgZHCTxTMbreYx3eG7viFOKuXEbWn1iQ==";
    private static String writeLocation = "West US";

    private static CosmosAsyncDatabase targetDatabase;
    private static CosmosAsyncContainer customContainer;
    private static AtomicBoolean resourcesCreated = new AtomicBoolean(false);

    public static void main( String[] args )
    {
        ConnectionPolicy defaultPolicy = ConnectionPolicy.getDefaultPolicy();
        defaultPolicy.setPreferredLocations(Lists.newArrayList(writeLocation));

        CosmosAsyncClient client = new CosmosClientBuilder()
                .setEndpoint(endpointUri)
                .setKey(primaryKey)
                .setConnectionPolicy(defaultPolicy)
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildAsyncClient();

        client.createDatabaseIfNotExists("Products").flatMap(databaseResponse -> {
            targetDatabase = databaseResponse.getDatabase();
            IndexingPolicy indexingPolicy = new IndexingPolicy();
            indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            indexingPolicy.setAutomatic(true);
            List<IncludedPath> includedPaths = new ArrayList<>();
            IncludedPath includedPath = new IncludedPath();
            includedPath.setPath("/*");
            includedPaths.add(includedPath);
            indexingPolicy.setIncludedPaths(includedPaths);  
            CosmosContainerProperties containerProperties = 
                new CosmosContainerProperties("Clothing", "/productId");
            containerProperties.setIndexingPolicy(indexingPolicy);
            return targetDatabase.createContainerIfNotExists(containerProperties, 10000); 
        }).flatMap(containerResponse -> {
            customContainer = containerResponse.getContainer();
            return Mono.empty();
        }).subscribe(voidItem -> {}, err -> {}, () -> {
            resourcesCreated.set(true);
        });

        while (!resourcesCreated.get());

        System.out.println(String.format("Database Id:\t%s",targetDatabase.getId()));
        System.out.println(String.format("Container Id:\t%s",customContainer.getId()));

        client.close();   
    }
}