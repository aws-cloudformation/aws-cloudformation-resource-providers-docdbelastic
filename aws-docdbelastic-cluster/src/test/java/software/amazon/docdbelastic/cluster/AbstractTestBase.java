package software.amazon.docdbelastic.cluster;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy LOGGER;
    protected static final boolean ENABLED;
    protected static final int PROXY_WAIT_TIME_SECONDS;
    protected static final String CLUSTER_NAME;
    protected static final String ADMIN_USER_PASSWORD;
    protected static final String AUTH_TYPE;
    protected static final Set<Tag> TAGS;
    protected static final Map<String, String> TAG_MAP;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        LOGGER = new LoggerProxy();
        PROXY_WAIT_TIME_SECONDS = 30;

        CLUSTER_NAME = "TestClusterName";
        ADMIN_USER_PASSWORD = "TestAdminUserPassword";
        AUTH_TYPE = "PLAIN_TEXT";

        TAGS = new HashSet<>();
        TAGS.add(Tag.builder().key("testKey1").value("testValue1").build());
        TAG_MAP = new HashMap<String, String>();
        TAG_MAP.put("testKey1", "testValue1");

        ENABLED = true;
    }

    static ProxyClient<DocDbElasticClient> MOCK_PROXY(
            AmazonWebServicesClientProxy proxy,
            DocDbElasticClient sdkClient) {

        return new ProxyClient<DocDbElasticClient>() {

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {

                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request,
                                              Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {

                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {

                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT,
                                                    Function<RequestT, ResponseInputStream<ResponseT>> function) {

                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT,
                                              Function<RequestT, ResponseBytes<ResponseT>> function) {

                throw new UnsupportedOperationException();
            }

            @Override
            public DocDbElasticClient client() {

                return sdkClient;
            }
        };
    }
}
