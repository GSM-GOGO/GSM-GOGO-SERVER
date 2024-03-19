package team.gsmgogo.domain.auth.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReissueTokenDto {
    private String accessToken;
    private String refreshToken;
}
