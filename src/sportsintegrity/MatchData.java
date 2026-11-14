package sportsintegrity;

public class MatchData {
    public final int fixtureId;
    public final int sportId;
    public final String sportName;
    public final int marginThreshold;
    public final int homeFacultyId;
    public final int awayFacultyId;
    public final int homeScore;
    public final int awayScore;
    public final String referee;
    public final Integer forfeitFacultyId;

    public MatchData(int fixtureId, int sportId, String sportName, int marginThreshold,
                     int homeFacultyId, int awayFacultyId, int homeScore, int awayScore,
                     String referee, Integer forfeitFacultyId) {
        this.fixtureId = fixtureId;
        this.sportId = sportId;
        this.sportName = sportName;
        this.marginThreshold = marginThreshold;
        this.homeFacultyId = homeFacultyId;
        this.awayFacultyId = awayFacultyId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.referee = referee;
        this.forfeitFacultyId = forfeitFacultyId;
    }

    public int margin() {
        return Math.abs(homeScore - awayScore);
    }

    public Integer winnerId() {
        return winnerOf(homeFacultyId, awayFacultyId, homeScore, awayScore);
    }

    /** Returns the winning faculty id, or null for a draw. */
    public static Integer winnerOf(int homeId, int awayId, int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) return homeId;
        if (awayGoals > homeGoals) return awayId;
        return null;
    }
}