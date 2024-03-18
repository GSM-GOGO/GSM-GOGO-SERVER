package team.gsmgogo.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import team.gsmgogo.global.config.GauthFeignConfig;
import team.gsmgogo.global.feign.dto.GauthTokenDto;
import team.gsmgogo.global.feign.dto.GauthTokenRequest;
import team.gsmgogo.global.feign.dto.GauthUserDto;

import java.net.URI;

@FeignClient(name = "GauthClient", configuration = GauthFeignConfig.class)
public interface GauthClient {

    @PostMapping
    GauthTokenDto getToken(URI baseUrl, @RequestBody GauthTokenRequest gauthTokenRequest);

    @GetMapping
    GauthUserDto getInfo(URI baseUrl, @RequestHeader("Authorization") String accessToken);

}