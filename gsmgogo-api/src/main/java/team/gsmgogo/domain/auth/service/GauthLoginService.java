package team.gsmgogo.domain.auth.service;

import team.gsmgogo.domain.auth.controller.dto.response.TokenDto;

public interface GauthLoginService {
    TokenDto execute(String code);
}
