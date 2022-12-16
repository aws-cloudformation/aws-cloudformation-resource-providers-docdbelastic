package software.amazon.docdbelastic.cluster;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-docdbelastic-cluster.json");
    }
    /**
     * This method overrides the BaseConfiguration method. resourceDefinedTags is called in the generated
     * HandlerWrapper.java class to obtain the resource level tags. These tags are combined with stack tags and set
     * in the ResourceHandlerRequest's desiredResourceTags and previousResourceTags
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags().stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));
        }
    }
}
