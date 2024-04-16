package team.gsmgogo.global.common;

public class CalculatePoint {
    public CalculatePointResponse execute(CalculatePointRequest req){
        CalculatePointResponse response = null;

        long winTeamPoint = req.getAScore() > req.getBScore() ? req.getAllAPoint() : req.getAllBPoint();
        long loseTeamPoint = req.getAScore() > req.getBScore() ? req.getAllBPoint() : req.getAllAPoint();

        // 스코어 예측 성공
        if (req.getAScore() == req.getBetAScore() && req.getBScore() == req.getBetBScore()){
            Long pointResult = (long) Math.ceil(((req.getBetPoint() * ((double) loseTeamPoint / winTeamPoint)) + req.getBetPoint()) * 1.5);
            response = new CalculatePointResponse(pointResult, null);
        }

        // 승부 예측 성공
        if ((req.getAScore() > req.getBScore() && req.getBetAScore() > req.getBetBScore()) ||
        (req.getAScore() < req.getBScore() && req.getBetAScore() < req.getBetBScore())){
            Long pointResult = (long) Math.ceil(((req.getBetPoint() * ((double) loseTeamPoint / winTeamPoint)) + req.getBetPoint()) * 0.9);
            response = new CalculatePointResponse(pointResult, null);
        }

        // 예측 실패
        if (response == null) {
            response = new CalculatePointResponse(null, req.getBetPoint());
        }

        return response;
    }
}