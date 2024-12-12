package vn.hoidanit.jobhunter.Util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import vn.hoidanit.jobhunter.domain.RestReponse;

@ControllerAdvice
public class FormatRestReponse implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
            Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        HttpServletResponse httpServletResponse = ((org.springframework.http.server.ServletServerHttpResponse) response)
                .getServletResponse();
        int status = httpServletResponse.getStatus();

        RestReponse<Object> restResponse = new RestReponse<Object>();
        restResponse.setStatusCode(status);

        if (body instanceof String) {
            return body;

        }
        if (status >= 400) {
            // case error
            return body;

        } else {
            // case success
            restResponse.setData(body);
            restResponse.setMessage("CALL API SUCCESS");
        }
        return restResponse;
    }

}
