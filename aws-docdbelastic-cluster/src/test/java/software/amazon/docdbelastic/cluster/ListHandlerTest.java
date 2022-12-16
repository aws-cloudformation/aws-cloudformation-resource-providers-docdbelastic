package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.ClusterInList;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersRequest;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private DocDbElasticClient sdkClient;

    @Mock
    private ProxyClient<DocDbElasticClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(LOGGER,
                MOCK_CREDENTIALS,
                () -> Duration.ofSeconds(PROXY_WAIT_TIME_SECONDS)
                        .toMillis());
        sdkClient = mock(DocDbElasticClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListClustersResponse mockListClustersResponse =
                ListClustersResponse.builder()
                        .clusters(ClusterInList.builder().clusterName("id1").build())
                        .build();

        when(proxyClient.client()
                .listClusters(any(ListClustersRequest.class)))
                .thenReturn(mockListClustersResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()
                .size()).isEqualTo(1);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NextToken() {

        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        String nextToken = "id2";

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        ListClustersResponse mockListClustersResponse =
                ListClustersResponse.builder()
                        .clusters(ClusterInList.builder().clusterName("id1").build())
                        .nextToken(nextToken)
                        .build();

        when(proxyClient.client()
                        .listClusters(any(ListClustersRequest.class)))
            .thenReturn(mockListClustersResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()
                           .size()).isEqualTo(1);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(nextToken);
    }

    @Test
    public void handleRequest_EmptyResponse() {

        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        String nextToken = "id1";

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        Collection<ClusterInList> clusters = Collections.emptyList();

        ListClustersResponse mockListClustersResponse =
                        ListClustersResponse.builder()
                                .clusters(clusters)
                                .nextToken(nextToken)
                                .build();

        when(proxyClient.client()
                .listClusters(any(ListClustersRequest.class)))
                .thenReturn(mockListClustersResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(nextToken);
    }
}
