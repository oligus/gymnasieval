package se.gymnasieval.workspace;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Flyttar headern X-Workspace-Key till GraphQL-kontexten, där controllers läser den med
 * {@code @ContextValue}. Motsvarar en event subscriber på kernel.request i Symfony.
 */
@Component
class WorkspaceKeyInterceptor implements WebGraphQlInterceptor {

    static final String HEADER = "X-Workspace-Key";
    static final String CONTEXT_KEY = "workspaceKey";

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String key = request.getHeaders().getFirst(HEADER);
        if (key != null && !key.isBlank()) {
            request.configureExecutionInput((input, builder) ->
                    builder.graphQLContext(Map.of(CONTEXT_KEY, key.strip())).build());
        }
        return chain.next(request);
    }
}
