package software.amazon.docdbelastic.cluster;

import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
    public static DocDbElasticClient getClient() {
        return DocDbElasticClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
