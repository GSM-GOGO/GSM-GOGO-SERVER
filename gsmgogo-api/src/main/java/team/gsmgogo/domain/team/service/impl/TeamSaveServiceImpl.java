package team.gsmgogo.domain.team.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.gsmgogo.domain.team.controller.dto.request.TeamSaveRequest;
import team.gsmgogo.domain.team.controller.dto.response.TeamClassType;
import team.gsmgogo.domain.team.entity.TeamEntity;
import team.gsmgogo.domain.team.repository.TeamJpaRepository;
import team.gsmgogo.domain.team.service.TeamSaveService;
import team.gsmgogo.domain.teamparticipate.entity.TeamParticipateEntity;
import team.gsmgogo.domain.teamparticipate.repository.TeamParticipateJpaRepository;
import team.gsmgogo.domain.user.entity.UserEntity;
import team.gsmgogo.domain.user.enums.ClassEnum;
import team.gsmgogo.domain.user.enums.Gender;
import team.gsmgogo.domain.user.repository.UserJpaRepository;
import team.gsmgogo.global.exception.error.ExpectedException;
import team.gsmgogo.global.facade.UserFacade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// TODO
//      축구: 남자 9명
//      배구: 남자 8명, 여자 1명 = 총 9명

@Service
@RequiredArgsConstructor
public class TeamSaveServiceImpl implements TeamSaveService {

    private final TeamJpaRepository teamJpaRepository;
    private final TeamParticipateJpaRepository teamParticipateJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserFacade userFacade;

    @Override
    @Transactional
    public void saveTeam(TeamSaveRequest request) {
        UserEntity currentUser = userFacade.getCurrentUser();

        if (teamJpaRepository.existsByTeamGradeAndTeamClassAndTeamType
                (currentUser.getUserGrade(), currentUser.getUserClass(), request.getTeamType()))
            throw new ExpectedException("이미 등록된 팀이 있습니다.", HttpStatus.BAD_REQUEST);

        Set<Long> chackDublicate = new HashSet<>();

        AtomicInteger participateCount = new AtomicInteger();
        request.getParticipates().forEach(
                participate -> {
                    if (!chackDublicate.add(participate.getUserId())) {
                        throw new ExpectedException("참가 인원이 중복되서는 안됩니다.", HttpStatus.BAD_REQUEST);
                    }

                    participateCount.getAndIncrement();
                }
        );

        if (participateCount.get() != 9)
            throw new ExpectedException("축구의 인원수는 9명입니다.", HttpStatus.BAD_REQUEST);

        request.getParticipates().stream().map(user -> {
            UserEntity findUser = userJpaRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new ExpectedException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

            if (
                    findUser.getUserGrade() != currentUser.getUserGrade() ||
                            toTeamClassType(findUser.getUserClass()) != toTeamClassType(currentUser.getUserClass())
            ) {
                throw new ExpectedException("참가 인원이 같은 학년, 과의 학생 확인해주세요.", HttpStatus.BAD_REQUEST);
            }

            return findUser;
        }).toList();

        TeamEntity newTeam = TeamEntity.builder()
                .teamName(request.getTeamName())
                .winCount(0)
                .isSurvived(true)
                .teamClass(currentUser.getUserClass())
                .teamGrade(currentUser.getUserGrade())
                .teamType(request.getTeamType())
                .build();

        TeamEntity savedTeam = teamJpaRepository.save(newTeam);

        List<TeamParticipateEntity> participates = request.getParticipates()
                .stream().map(user -> TeamParticipateEntity.builder()
                        .team(savedTeam)
                        .user(getUser(user.getUserId()))
                        .positionX(user.getPositionX())
                        .positionY(user.getPositionY())
                        .build()
                ).toList();

        teamParticipateJpaRepository.saveAll(participates);

    }

    private TeamClassType toTeamClassType(ClassEnum classEnum) {
        if (classEnum == ClassEnum.ONE || classEnum == ClassEnum.TWO)
            return TeamClassType.SW;
        else
            return TeamClassType.EB;
    }

    private UserEntity getUser(Long userId) {
        UserEntity user = userJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (user.getGender() != Gender.MALE)
            throw new ExpectedException("축구 경기는 남학생만 참여 가능합니다.", HttpStatus.BAD_REQUEST);

        return user;
    }

}
