package software.amazon.docdbelastic.cluster;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.DocDbElasticClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;

public class TagHelper {

    protected static Set<Tag> convertToCfnTags(final Map<String, String> tags) {

        return tags.entrySet().stream()
            .map( entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
            .collect(Collectors.toSet());
    }

    /**
     * Generate tags to put into resource creation request. This includes user defined tags and system tags as well.
     *
     * @param handlerRequest Create handler request
     *
     * @return Map of tags to add at domain creation
     */
    protected static Map<String, String> generateTagsForCreate(final ResourceHandlerRequest<ResourceModel> request) {
        final Map<String, String> tagMap = new HashMap<>();

        // TODO: uncomment when system tags are supported
        // if (request.getSystemTags() != null) {
        //     tagMap.putAll(request.getSystemTags());
        // }

        if (request.getDesiredResourceTags() != null) {
            tagMap.putAll(request.getDesiredResourceTags());
        }
        return Collections.unmodifiableMap(tagMap);
    }

    /**
     * Generate
     *
     * @param handlerRequest Create handler request
     *
     * @return Map of tags to add at domain creation
     */
    protected static Map<String, String> getPreviousTagsForUpdate(final ResourceHandlerRequest<ResourceModel> request) {
        return request.getPreviousResourceTags() == null ? Collections.emptyMap() : request.getPreviousResourceTags();
    }

    /**
     * Generate
     *
     * @param handlerRequest Create handler request
     *
     * @return Map of tags to add at domain creation
     */
    protected static Map<String, String> getDesiredTagsForUpdate(final ResourceHandlerRequest<ResourceModel> request) {
        return request.getDesiredResourceTags() == null ? Collections.emptyMap() : request.getDesiredResourceTags();
    }

    /**
     * Determines the tags the customer desired to add
     *
     * @param previousTags
     * @param desiredTags
     *
     * @return Map of tags to add to domain
     */
    protected static Map<String, String> generateTagsToAdd(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        return desiredTags.entrySet().stream()
            .filter(e -> !previousTags.containsKey(e.getKey()) || !Objects.equals(previousTags.get(e.getKey()), e.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

    /**
     * Determines the tags the customer desired to remove
     *
     * @param previousTags
     * @param desiredTags
     *
     * @return Set of tag keys to remove from domain
     */
    protected static Set<String> generateTagsToRemove(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        final Set<String> desiredTagNames = desiredTags.keySet();

        return previousTags.keySet().stream()
            .filter(tagName -> !desiredTagNames.contains(tagName))
            .collect(Collectors.toSet());
    }

    protected static ProgressEvent<ResourceModel, CallbackContext> tagResource(
        final AmazonWebServicesClientProxy proxy,
        final ProxyClient<DocDbElasticClient> serviceClient,
        final ResourceModel resourceModel,
        final ResourceHandlerRequest<ResourceModel> handlerRequest,
        final CallbackContext callbackContext,
        final Map<String, String> addedTags,
        final Logger logger) {

        logger.log(String.format("[UPDATE][IN PROGRESS] Going to add tags for ... resource: %s",
            resourceModel.getClusterArn()));

        return proxy.initiate("AWS-DocDBElastic-DBCluster::TagOps", serviceClient, resourceModel, callbackContext)
            .translateToServiceRequest(model ->
                RequestTranslator.translateToTagRequest(model, addedTags))
            .makeServiceCall((request, client) -> {
                try {
                    return proxy.injectCredentialsAndInvokeV2(request, client.client()::tagResource);
                } catch (AwsServiceException e) {
                    throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                }
            })
            .progress();
    }

    protected static ProgressEvent<ResourceModel, CallbackContext> untagResource(
        final AmazonWebServicesClientProxy proxy,
        final ProxyClient<DocDbElasticClient> serviceClient,
        final ResourceModel resourceModel,
        final ResourceHandlerRequest<ResourceModel> handlerRequest,
        final CallbackContext callbackContext,
        final Set<String> removedTags,
        final Logger logger) {

        logger.log(String.format("[UPDATE][IN PROGRESS] Going to remove tags for ... resource: %s",
            resourceModel.getClusterArn()));

        return proxy.initiate("AWS-DocDBElastic-DBCluster::TagOps", serviceClient, resourceModel, callbackContext)
            .translateToServiceRequest(model ->
                RequestTranslator.translateToUntagRequest(model, removedTags))
            .makeServiceCall((request, client) -> {
                try {
                    return proxy.injectCredentialsAndInvokeV2(request, client.client()::untagResource);
                } catch (AwsServiceException e) {
                    throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                }
            })
            .progress();
    }

}
