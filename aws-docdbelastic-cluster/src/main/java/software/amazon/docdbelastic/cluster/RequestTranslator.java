package software.amazon.docdbelastic.cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;
import software.amazon.awssdk.services.docdbelastic.model.CreateClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.DeleteClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersRequest;
import software.amazon.awssdk.services.docdbelastic.model.UpdateClusterRequest;
import software.amazon.awssdk.services.docdbelastic.model.TagResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.UntagResourceRequest;
import software.amazon.awssdk.services.docdbelastic.model.ListTagsForResourceRequest;

import software.amazon.awssdk.utils.StringUtils;

public class RequestTranslator {
    static CreateClusterRequest translateToCreateRequest(ResourceModel model, Map<String, String> tags) {
        return CreateClusterRequest.builder()
                .clusterName(model.getClusterName())
                .authType(model.getAuthType())
                .adminUserName(model.getAdminUserName())
                .adminUserPassword(model.getAdminUserPassword())
                .shardCapacity(model.getShardCapacity())
                .shardCount(model.getShardCount())
                .vpcSecurityGroupIds(model.getVpcSecurityGroupIds())
                .subnetIds(model.getSubnetIds())
                .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow())
                .kmsKeyId(model.getKmsKeyId())
                .tags(tags)
                .build();
    }

    static GetClusterRequest translateToReadRequest(ResourceModel model) {
        return GetClusterRequest.builder()
                .clusterArn(model.getClusterArn())
                .build();
    }

    static DeleteClusterRequest translateToDeleteRequest(ResourceModel model) {
        return DeleteClusterRequest.builder()
                .clusterArn(model.getClusterArn())
                .build();
    }

    static ListClustersRequest translateToListRequest(String nextToken) {
        return ListClustersRequest.builder()
                .maxResults(50)
                .nextToken(nextToken)
                .build();
    }

    static UpdateClusterRequest translateToUpdateClusterRequest(ResourceModel oldModel, ResourceModel newModel) {
        // If all fields are the same, return null, meaning no need to call UpdateCluster
        if (StringUtils.equals(oldModel.getAuthType(), newModel.getAuthType()) &&
            oldModel.getShardCapacity() == newModel.getShardCapacity() &&
            oldModel.getShardCount() == newModel.getShardCount() &&
            areListsTheSameIgnoreOrder(oldModel.getVpcSecurityGroupIds(), newModel.getVpcSecurityGroupIds()) &&
            areListsTheSameIgnoreOrder(oldModel.getSubnetIds(), newModel.getSubnetIds()) &&
            StringUtils.equals(oldModel.getAdminUserPassword(), newModel.getAdminUserPassword())
        ) {
            return null;
        }
        return UpdateClusterRequest.builder()
                .clusterArn(oldModel.getClusterArn())
                .authType(newModel.getAdminUserPassword() == null ? null : newModel.getAuthType())
                .adminUserPassword(newModel.getAdminUserPassword())
                .shardCapacity(oldModel.getShardCapacity() == newModel.getShardCapacity() ? null : newModel.getShardCapacity())
                .shardCount(oldModel.getShardCount() == newModel.getShardCount() ? null : newModel.getShardCount())
                .vpcSecurityGroupIds(areListsTheSameIgnoreOrder(oldModel.getVpcSecurityGroupIds(), newModel.getVpcSecurityGroupIds()) ? null : newModel.getVpcSecurityGroupIds())
                .subnetIds(areListsTheSameIgnoreOrder(oldModel.getSubnetIds(), newModel.getSubnetIds()) ? null : newModel.getSubnetIds())
                .build();
    }

    static TagResourceRequest translateToTagRequest(ResourceModel model, Map<String, String> addedTags) {
        return TagResourceRequest.builder()
                .resourceArn(model.getClusterArn())
                .tags(addedTags)
                .build();
    }

    static UntagResourceRequest translateToUntagRequest(ResourceModel model, Set<String> removedTags) {
        return UntagResourceRequest.builder()
                .resourceArn(model.getClusterArn())
                .tagKeys(removedTags)
                .build();
    }

    static ListTagsForResourceRequest translateToListTagsRequest(String resourceArn) {
        return ListTagsForResourceRequest.builder()
                .resourceArn(resourceArn)
                .build();
    }

    static boolean areListsTheSameIgnoreOrder(List<String> a, List<String> b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        return a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }
}
