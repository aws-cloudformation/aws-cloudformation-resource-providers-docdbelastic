package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Auth;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.ConflictException;
import software.amazon.awssdk.services.docdbelastic.model.CreateClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.CreateClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.Status;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

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
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .adminUserPassword(ADMIN_USER_PASSWORD)
                .build();

        final ResourceModel desiredOutputModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .vpcSecurityGroupIds(new ArrayList<>())
                .subnetIds(new ArrayList<>())
                .tags(TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(TAG_MAP)
                .build();

        final CreateClusterResponse mockCreateClusterResponse =
                CreateClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .authType(Auth.PLAIN_TEXT)
                                .build()
                        )
                        .build();

        final ListTagsForResourceResponse mockListTagsForResourceResponse =
                ListTagsForResourceResponse.builder().tags(TAG_MAP).build();

        final GetClusterResponse mockGetClusterResponse =
                GetClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .authType("PLAIN_TEXT")
                                .status(Status.ACTIVE)
                                .build()
                        )
                        .build();

        when(proxyClient.client()
                .listTagsForResource(any(ListTagsForResourceRequest.class))).thenReturn(
                mockListTagsForResourceResponse);
        when(proxyClient.client()
                .createCluster(any(CreateClusterRequest.class))).thenReturn(
                mockCreateClusterResponse);
        when(proxyClient.client()
                .getCluster(any(GetClusterRequest.class))).thenReturn(
                mockGetClusterResponse);


        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredOutputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        // shouldn't need to call create more than once
        verify(sdkClient, times(1)).createCluster(any(CreateClusterRequest.class));

        // at least once for stabilization, once for read operation on success
        verify(sdkClient,
                atLeast(2)).getCluster(any(GetClusterRequest.class));
    }

    @Test
    public void handleRequest_InProgress() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .adminUserPassword(ADMIN_USER_PASSWORD)
                .build();

        final ResourceModel desiredOutputModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .adminUserPassword(ADMIN_USER_PASSWORD)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CreateClusterResponse mockCreateClusterResponse =
                CreateClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .status(Status.CREATING)
                                .build()
                        )
                        .build();

        final GetClusterResponse mockGetClusterResponse =
                GetClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .status(Status.CREATING)
                                .build()
                        )
                        .build();

        when(proxyClient.client()
                .createCluster(any(CreateClusterRequest.class))).thenReturn(
                mockCreateClusterResponse);
        when(proxyClient.client()
                .getCluster(any(GetClusterRequest.class))).thenReturn(
                mockGetClusterResponse);


        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);
        LOGGER.log(response.toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);
        assertThat(response.getResourceModel()).isEqualTo(desiredOutputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        // shouldn't need to call create more than once
        verify(sdkClient, times(1)).createCluster(any(CreateClusterRequest.class));

        // once for stabilization
        verify(sdkClient,
                times(1)).getCluster(any(GetClusterRequest.class));
    }

    @Test
    public void handleRequest_ClusterAlreadyExistsError() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .adminUserPassword(ADMIN_USER_PASSWORD)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .createCluster(any(CreateClusterRequest.class))).thenThrow(
                ConflictException.class);

        assertThatExceptionOfType(ResourceAlreadyExistsException.class).isThrownBy(() ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER));
    }

    @Test
    public void handleRequest_GeneralServiceError() {

        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .adminUserPassword(ADMIN_USER_PASSWORD)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .createCluster(any(CreateClusterRequest.class))).thenThrow(
                AwsServiceException.class);

        assertThatExceptionOfType(CfnGeneralServiceException.class).isThrownBy(() ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER));
    }
}
