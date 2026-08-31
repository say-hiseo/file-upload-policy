package com.assignment.fileuploadpolicy.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileUploadPolicyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Upload Policy API")
                        .description("확장자 차단 정책 관리 및 파일 업로드 API. "
                                + "정책 판단 기준(A)과 업로드 검증(B)이 동일한 조회 로직을 공유합니다.")
                        .version("v1"));
    }
}