package software.amazon.docdbelastic.cluster;

import java.util.Set;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<DocDbElasticClient> proxyClient,
            Logger logger) {

        this.logger = logger;

        return proxy.initiate(CALL_GRAPH_PREFIX + "Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(RequestTranslator::translateToReadRequest)
                .makeServiceCall((awsRequest, client) -> {
                    GetClusterResponse awsResponse = null;
                    try {
                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::getCluster);
                    } catch (AwsServiceException e) {
                        throw ExceptionTranslator.translateFromServiceException(e);
                    }

                    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .done(awsResponse -> {
                    String resourceArn = awsResponse.cluster().clusterArn();
                    ListTagsForResourceResponse listTagsForResourceResponse =
                        proxyClient.injectCredentialsAndInvokeV2(RequestTranslator.translateToListTagsRequest(resourceArn),
                                                                 proxyClient.client()::listTagsForResource);
                    Set<Tag> tags = TagHelper.convertToCfnTags(listTagsForResourceResponse.tags());
                    ResourceModel resourceModel = ResponseTranslator.translateFromReadResponse(awsResponse, tags);

                    logger.log(resourceModel.toString());
                    return ProgressEvent.defaultSuccessHandler(resourceModel);
                });
    }
}
