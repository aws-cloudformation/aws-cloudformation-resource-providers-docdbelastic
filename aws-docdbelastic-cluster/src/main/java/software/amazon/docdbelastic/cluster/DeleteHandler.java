package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.DeleteClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

public class DeleteHandler extends BaseHandlerStd {
    protected static final Constant DELETE_BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60L)).delay(Duration.ofSeconds(30L)).build();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            ProxyClient<DocDbElasticClient> proxyClient,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH_PREFIX + "Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(RequestTranslator::translateToDeleteRequest)
                                .backoffDelay(DELETE_BACKOFF_STRATEGY)
                                .makeServiceCall((awsRequest, client) -> {
                                    DeleteClusterResponse awsResponse = null;

                                    Cluster clusterStateSoFar = callbackContext.getCluster();

                                    if (clusterStateSoFar == null) {
                                        try {
                                            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest,
                                                    client.client()::deleteCluster);
                                        } catch (AwsServiceException e) {
                                            throw ExceptionTranslator.translateFromServiceException(e);
                                        }

                                        logger.log(String.format("%s deletion request successfully sent.", ResourceModel.TYPE_NAME));
                                    } else {
                                        logger.log(String.format("%s state is: %s", ResourceModel.TYPE_NAME, clusterStateSoFar.statusAsString()));
                                    }
                                    return awsResponse;
                                })
                                .stabilize((awsRequest, awsResponse, client, model, context) ->
                                        new ResourceStabilizer(client, logger).stabilizeDelete(model, context))
                                .progress()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
