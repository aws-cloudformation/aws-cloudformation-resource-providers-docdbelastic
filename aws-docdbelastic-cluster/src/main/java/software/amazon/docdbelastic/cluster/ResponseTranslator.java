package software.amazon.docdbelastic.cluster;

import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.awssdk.services.docdbelastic.model.GetClusterResponse;
import software.amazon.awssdk.services.docdbelastic.model.ListClustersResponse;

public class ResponseTranslator {
    static ResourceModel translateFromReadResponse(GetClusterResponse response, Set<Tag> tags) {

        Cluster cluster = response.cluster();

        return ResourceModel.builder()
                .clusterArn(cluster.clusterArn())
                .clusterName(cluster.clusterName())
                .adminUserName(cluster.adminUserName())
                .authType(cluster.authType().toString())
                .shardCapacity(cluster.shardCapacity())
                .shardCount(cluster.shardCount())
                .vpcSecurityGroupIds(cluster.vpcSecurityGroupIds())
                .subnetIds(cluster.subnetIds())
                .preferredMaintenanceWindow(cluster.preferredMaintenanceWindow())
                .kmsKeyId(cluster.kmsKeyId())
                .tags(tags)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param response the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(ListClustersResponse response) {
        return Utils.streamOfOrEmpty(response.clusters())
                .map(resource -> ResourceModel.builder()
                        .clusterArn(resource.clusterArn())
                        .build())
                .collect(Collectors.toList());
    }
}
