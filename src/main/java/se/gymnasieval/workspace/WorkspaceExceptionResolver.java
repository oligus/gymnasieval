package se.gymnasieval.workspace;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * Gör om undantag till tydliga GraphQL-fel med typ (extensions.classification),
 * i stället för det generiska "INTERNAL_ERROR".
 */
@Component
class WorkspaceExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof WorkspaceAccessDeniedException denied) {
            type = denied.isReadOnly() ? ErrorType.FORBIDDEN : ErrorType.UNAUTHORIZED;
        } else if (ex instanceof IllegalArgumentException) {
            type = ErrorType.BAD_REQUEST;
        } else {
            return null; // låt Spring hantera övriga fel
        }
        return GraphQLError.newError()
                .errorType(type)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}
