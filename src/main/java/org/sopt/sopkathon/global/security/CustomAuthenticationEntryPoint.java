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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public CustomAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        writeUnauthorized(request, response);
    }

    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {
        writeUnauthorized(request, response);
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityErrorCode errorCode = SecurityErrorCode.AUTH_UNAUTHORIZED;
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
