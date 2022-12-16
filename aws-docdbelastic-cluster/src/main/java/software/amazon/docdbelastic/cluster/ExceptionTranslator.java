package software.amazon.docdbelastic.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.docdbelastic.model.InternalServerException;
import software.amazon.awssdk.services.docdbelastic.model.AccessDeniedException;
import software.amazon.awssdk.services.docdbelastic.model.ConflictException;
import software.amazon.awssdk.services.docdbelastic.model.ResourceNotFoundException;
import software.amazon.awssdk.services.docdbelastic.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.docdbelastic.model.ThrottlingException;
import software.amazon.awssdk.services.docdbelastic.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

public class ExceptionTranslator {
    /**
     * Translates docdbelastic exception to corresponding cfn exception
     *
     * @param e exception
     *
     * @return the cfn exception most closely mapped from the service exception
     */
    static BaseHandlerException translateFromServiceException(final AwsServiceException e) {
        if (e instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(e);
        } else if (e instanceof ConflictException) {
            return new ResourceAlreadyExistsException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        } else if (e instanceof ServiceQuotaExceededException) {
            return new CfnServiceLimitExceededException(e);
        } else if (e instanceof ThrottlingException) {
            return new CfnThrottlingException(e);
        } else if (e instanceof ValidationException) {
            return new CfnInvalidRequestException(e);
        } else if (e instanceof InternalServerException) {
            return new CfnInternalFailureException(e);
        } else {
            return new CfnGeneralServiceException(e);
        }
    }
}
