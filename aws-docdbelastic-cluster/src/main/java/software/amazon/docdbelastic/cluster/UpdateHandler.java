package software.amazon.docdbelastic.cluster;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.ConflictException;
import software.amazon.awssdk.services.docdbelastic.model.UpdateClusterRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

public class UpdateHandler extends BaseHandlerStd {
    protected static final Constant UPDATE_BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60L)).delay(Duration.ofSeconds(30L)).build();


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            ProxyClient<DocDbElasticClient> proxyClient,
            final Logger logger) {

        final Map<String, String> previousTags = TagHelper.getPreviousTagsForUpdate(request);
        final Map<String, String> desiredTags = TagHelper.getDesiredTagsForUpdate(request);

        if (request == null) {
            logger.log("Request is null. Throwing exception.");
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME);
        }

        UpdateClusterRequest updateClusterRequest =
            RequestTranslator.translateToUpdateClusterRequest(request.getPreviousResourceState(), request.getDesiredResourceState());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> {
                        if (updateClusterRequest == null) {
                            return ProgressEvent.progress(progress.getResourceModel(), callbackContext);
                        }
                        return proxy.initiate(CALL_GRAPH_PREFIX + "Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(model -> updateClusterRequest)
                                .backoffDelay(UPDATE_BACKOFF_STRATEGY)
                                .makeServiceCall((awsRequest, client) -> {
                                    AwsResponse awsResponse = null;
                                    Cluster clusterStateSoFar = callbackContext.getCluster();

                                    if (clusterStateSoFar == null) {
                                        try {
                                            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest,
                                                    client.client()::updateCluster);
                                        } catch (AwsServiceException e) {
                                            throw ExceptionTranslator.translateFromServiceException(e);
                                        }

                                        logger.log(String.format("%s update request successfully sent.", ResourceModel.TYPE_NAME));
                                    } else {
                                        logger.log(String.format("%s state is: %s", ResourceModel.TYPE_NAME, clusterStateSoFar.statusAsString()));
                                    }

                                    return awsResponse;
                                })
                                .stabilize((awsRequest, awsResponse, client, model, context) ->
                                        new ResourceStabilizer(client, logger).stabilizeUpdate(model, context))
                                .progress();
                })
                .then(progress -> {
                    final Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);
                    if (tagsToAdd.isEmpty()) {
                        return ProgressEvent.progress(progress.getResourceModel(), callbackContext);
                    }
                    return TagHelper.tagResource(proxy, proxyClient, request.getDesiredResourceState(), request,
                                                    callbackContext, tagsToAdd, logger);
                })
                .then(progress -> {
                    Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);
                    if (tagsToRemove.isEmpty()) {
                        return ProgressEvent.progress(progress.getResourceModel(), callbackContext);
                    }
                    return TagHelper.untagResource(proxy, proxyClient, request.getDesiredResourceState(), request,
                                                    callbackContext, tagsToRemove, logger);
                })
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
