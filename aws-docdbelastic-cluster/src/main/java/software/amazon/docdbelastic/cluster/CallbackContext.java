package software.amazon.docdbelastic.cluster;

import software.amazon.awssdk.services.docdbelastic.model.Cluster;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private Cluster cluster;
}
