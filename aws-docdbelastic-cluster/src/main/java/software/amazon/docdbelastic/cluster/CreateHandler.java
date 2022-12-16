package software.amazon.docdbelastic.cluster;


import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.ConflictException;
import software.amazon.awssdk.services.docdbelastic.model.CreateClusterResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

public class CreateHandler extends BaseHandlerStd {
    protected static final Constant CREATE_BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60L)).delay(Duration.ofSeconds(30L)).build();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            ProxyClient<DocDbElasticClient> proxyClient,
            final Logger logger) {

        final ResourceModel resourceModel = request.getDesiredResourceState();
        final Map<String, String> tagsToCreate = TagHelper.generateTagsForCreate(request);

        if (resourceModel.getAdminUserPassword() == null || resourceModel.getAdminUserPassword().isEmpty()) {
            throw new CfnInvalidRequestException("required key [AdminUserPassword] not found");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(CALL_GRAPH_PREFIX + "Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest((model) -> RequestTranslator.translateToCreateRequest(model, tagsToCreate))
                        .backoffDelay(CREATE_BACKOFF_STRATEGY)
                        .makeServiceCall((awsRequest, client) -> {
                            AwsResponse awsResponse = null;
                            Cluster clusterStateSoFar = callbackContext.getCluster();

                            if (clusterStateSoFar == null) {
                                try {
                                    CreateClusterResponse response = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::createCluster);

                                    resourceModel.setClusterArn(response.cluster().clusterArn());
                                } catch (AwsServiceException e) {
                                    throw ExceptionTranslator.translateFromServiceException(e);
                                }
                                logger.log(String.format("%s creation request successfully sent.", ResourceModel.TYPE_NAME));
                            } else {
                                logger.log(String.format("%s state is: %s", ResourceModel.TYPE_NAME, clusterStateSoFar.statusAsString()));
                            }
                            return awsResponse;
                        })
                        .stabilize((awsRequest, awsResponse, client, model, context) ->
                                new ResourceStabilizer(client, logger).stabilizeCreate(model, context))
                        .progress()

                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
