package software.amazon.docdbelastic.cluster;

import java.util.List;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersRequest;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            ProxyClient<DocDbElasticClient> proxyClient,
            final Logger logger) {

        final ListClustersRequest awsRequest = RequestTranslator.translateToListRequest(request.getNextToken());

        ListClustersResponse awsResponse = proxy.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::listClusters);

        String nextToken = awsResponse.nextToken();

        final List<ResourceModel> models = ResponseTranslator.translateFromListResponse(awsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
