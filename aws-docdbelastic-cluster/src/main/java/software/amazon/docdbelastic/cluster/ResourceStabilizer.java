package software.amazon.docdbelastic.cluster;

import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException;
import software.amazon.awssdk.services.docdbelastic.model.Status;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

public class ResourceStabilizer {
    private ProxyClient<DocDbElasticClient> proxyClient;
    private Logger logger;

    public ResourceStabilizer(ProxyClient<DocDbElasticClient> proxyClient, Logger logger) {

        this.proxyClient = proxyClient;
        this.logger = logger;
    }

    public boolean stabilizeCreate(ResourceModel model, CallbackContext context) {
        if (model == null) {
            return false;
        }

        boolean stabilized = stabilizeCreateOrUpdate(model, context);
        logger.log(String.format("%s [%s] creation has stabilized: %s", ResourceModel.TYPE_NAME,
                model.getPrimaryIdentifier(), stabilized));
        return stabilized;
    }

    public boolean stabilizeUpdate(ResourceModel model, CallbackContext context) {

        if (model == null) {
            return false;
        }

        boolean stabilized = stabilizeCreateOrUpdate(model, context);

        logger.log(String.format("%s [%s] updating has stabilized: %s", ResourceModel.TYPE_NAME,
                model.getPrimaryIdentifier(), stabilized));
        return stabilized;
    }

    private boolean stabilizeCreateOrUpdate(ResourceModel model, CallbackContext context) {
        GetClusterResponse response = proxyClient.injectCredentialsAndInvokeV2(
                RequestTranslator.translateToReadRequest(model),
                proxyClient.client()::getCluster);

        context.setCluster(response.cluster());
        return Status.ACTIVE == response.cluster().status();
    }

    public boolean stabilizeDelete(ResourceModel model, CallbackContext context) {
        boolean stabilized = false;
        GetClusterRequest readRequest =
                RequestTranslator.translateToReadRequest(model);

        try {
            GetClusterResponse response = proxyClient.injectCredentialsAndInvokeV2(readRequest, proxyClient.client()::getCluster);
            context.setCluster(response.cluster());
        } catch (ResourceNotFoundException e) {
            stabilized = true;
        }
        logger.log(String.format("%s [%s] deletion has stabilized: %s",
                ResourceModel.TYPE_NAME,
                model.getPrimaryIdentifier(),
                stabilized));
        return stabilized;
    }
}
