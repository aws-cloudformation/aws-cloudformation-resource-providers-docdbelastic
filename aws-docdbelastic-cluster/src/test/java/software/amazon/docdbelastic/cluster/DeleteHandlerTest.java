package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.DeleteClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.DeleteClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException;
import software.amazon.awssdk.services.docdbelastic.model.Status;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

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
        final DeleteHandler handler = new DeleteHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .build();

        DeleteClusterResponse mockDeleteClusterResponse =
                DeleteClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .status(Status.DELETING)
                                .build())
                        .build();

        when(proxyClient.client()
                .getCluster(any(GetClusterRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client()
                .deleteCluster(any(DeleteClusterRequest.class)))
                .thenReturn(mockDeleteClusterResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ResourceNotFoundException() {
        DeleteHandler handler = new DeleteHandler();

        ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .deleteCluster(any(DeleteClusterRequest.class)))
                .thenThrow(software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException.class);

        assertThatExceptionOfType(CfnNotFoundException.class).isThrownBy(() ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER));
    }
}
