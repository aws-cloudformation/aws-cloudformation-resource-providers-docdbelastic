package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Auth;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.Status;
import software.amazon.awssdk.services.docdbelastic.model.UpdateClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.UpdateClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.docdbelastic.model.TagResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.TagResourceResponse;
import software.amazon.awssdk.services.docdbelastic.model.UntagResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.UntagResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel newModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .shardCapacity(4)
                .vpcSecurityGroupIds(new ArrayList<>())
                .subnetIds(new ArrayList<>())
                .tags(Collections.emptySet())
                .build();

        final ResourceModel oldModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .shardCapacity(2)
                .vpcSecurityGroupIds(new ArrayList<>())
                .subnetIds(new ArrayList<>())
                .tags(Collections.emptySet())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(newModel)
                .previousResourceState(oldModel)
                .build();

        GetClusterResponse mockGetClusterResponse =
                GetClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .authType(Auth.PLAIN_TEXT)
                                .shardCapacity(4)
                                .status(Status.ACTIVE)
                                .build())
                        .build();

        UpdateClusterResponse mockUpdateClusterResponse =
                UpdateClusterResponse.builder()
                        .cluster(Cluster.builder().build())
                        .build();

        ListTagsForResourceResponse mockListTagsForResourceResponse =
                ListTagsForResourceResponse.builder().tags(new HashMap<>()).build();

        when(proxyClient.client()
                .getCluster(any(GetClusterRequest.class)))
                .thenReturn(mockGetClusterResponse);
        when(proxyClient.client()
                .updateCluster(any(UpdateClusterRequest.class)))
                .thenReturn(mockUpdateClusterResponse);
        when(proxyClient.client()
                .listTagsForResource(any(ListTagsForResourceRequest.class)))
                .thenReturn(mockListTagsForResourceResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessWithTagging() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel newModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .vpcSecurityGroupIds(new ArrayList<>())
                .subnetIds(new ArrayList<>())
                .tags(Collections.emptySet())
                .build();

        final ResourceModel oldModel = ResourceModel.builder()
                .clusterName(CLUSTER_NAME)
                .authType(AUTH_TYPE)
                .vpcSecurityGroupIds(new ArrayList<>())
                .subnetIds(new ArrayList<>())
                .tags(Collections.emptySet())
                .build();

        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder()
                        .desiredResourceState(newModel)
                        .previousResourceState(oldModel)
                        .build();

        final Map<String, String> oldTag = TAG_MAP;
        final Map<String, String> newTag = new HashMap<>();
        newTag.put("testKey2", "testValue2");

        request.setPreviousResourceTags(oldTag);
        request.setDesiredResourceTags(newTag);

        GetClusterResponse mockGetClusterResponse =
                GetClusterResponse.builder()
                        .cluster(Cluster.builder()
                                .clusterName(CLUSTER_NAME)
                                .authType(Auth.PLAIN_TEXT)
                                .status(Status.ACTIVE)
                                .build())
                        .build();

        UpdateClusterResponse mockUpdateClusterResponse =
                UpdateClusterResponse.builder()
                        .cluster(Cluster.builder().build())
                        .build();

        ListTagsForResourceResponse mockListTagsForResourceResponse =
                ListTagsForResourceResponse.builder().tags(new HashMap<>()).build();

        when(proxyClient.client()
                .getCluster(any(GetClusterRequest.class)))
                .thenReturn(mockGetClusterResponse);
        when(proxyClient.client()
                .updateCluster(any(UpdateClusterRequest.class)))
                .thenReturn(mockUpdateClusterResponse);
        when(proxyClient.client()
                .listTagsForResource(any(ListTagsForResourceRequest.class)))
                .thenReturn(mockListTagsForResourceResponse);
        when(proxyClient.client()
                .tagResource(any(TagResourceRequest.class)))
                .thenReturn(TagResourceResponse.builder().build());
        when(proxyClient.client()
                .untagResource(any(UntagResourceRequest.class)))
                .thenReturn(UntagResourceResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, LOGGER);

        verify(proxyClient.client()).updateCluster(any(UpdateClusterRequest.class));
        verify(proxyClient.client()).tagResource(any(TagResourceRequest.class));
        verify(proxyClient.client()).untagResource(any(UntagResourceRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
