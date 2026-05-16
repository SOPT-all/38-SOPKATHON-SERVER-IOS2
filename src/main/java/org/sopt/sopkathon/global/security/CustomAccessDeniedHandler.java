package org.sopt.sopkathon.global.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.sopt.sopkathon.global.error.FieldErrorResponse;
import org.sopt.sopkathon.global.error.GlobalErrorResponse;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public CustomAccessDeniedHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        SecurityErrorCode errorCode = SecurityErrorCode.AUTH_FORBIDDEN;
        GlobalErrorResponse body = new GlobalErrorResponse(
                false,
                errorCode.httpStatus().value(),
                errorCode.code(),
                errorCode.message(),
                request.getRequestURI(),
                traceId(request, response),
                Instant.now(),
                List.<FieldErrorResponse>of()
        );
        response.setStatus(errorCode.httpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getWriter(), body);
    }

    private String traceId(HttpServletRequest request, HttpServletResponse response) {
        Object traceId = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        String resolved = traceId == null ? UUID.randomUUID().toString() : String.valueOf(traceId);
        response.setHeader(TraceIdFilter.TRACE_ID_HEADER, resolved);
        return resolved;
    }
}
