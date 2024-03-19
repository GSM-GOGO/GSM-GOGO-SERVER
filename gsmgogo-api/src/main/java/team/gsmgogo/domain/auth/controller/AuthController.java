package team.gsmgogo.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.gsmgogo.domain.auth.controller.dto.response.AuthCallBackCodeResponse;
import team.gsmgogo.domain.auth.controller.dto.response.TokenDto;
import team.gsmgogo.domain.auth.service.GauthLoginService;
import team.gsmgogo.global.facade.UserFacade;
import team.gsmgogo.global.manager.CookieManager;
import team.gsmgogo.global.security.jwt.JwtTokenProvider;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GauthLoginService gauthLoginService;
    private final CookieManager cookieManager;
    private final UserFacade userFacade;

    @Value("${gauth.clientId}")
    private String clientId;
    @Value("${gauth.redirectUrl}")
    private String redirectUri;
    @Value("${spring.jwt.accessExp}")
    public Long accessExp;
    @Value("${spring.jwt.refreshExp}")
    public Long refreshExp;


    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String loginUrl = "https://gauth.co.kr/login?" +
                "client_id=" + clientId + "&" +
                "redirect_uri=" + redirectUri;

        response.sendRedirect(loginUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<AuthCallBackCodeResponse> callback(@RequestParam("code") String code, HttpServletResponse response) {
        TokenDto tokenDto = gauthLoginService.execute(code);
        cookieManager.addTokenCookie(response, JwtTokenProvider.ACCESS_KEY, tokenDto.getAccessToken(), accessExp, true);
        cookieManager.addTokenCookie(response, JwtTokenProvider.REFRESH_KEY, tokenDto.getRefreshToken(), refreshExp, true);
        return ResponseEntity.ok(new AuthCallBackCodeResponse(tokenDto.getIsSignup()));
    }

}
