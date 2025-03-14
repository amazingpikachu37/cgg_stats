package Threads;

import java.io.Serializable;

public enum ParticipationDuration implements Serializable {

    YEAR, MONTH, DAY;
    public String toCapitalizedString() {
        return this.toString().charAt(0)+this.toString().substring(1).toLowerCase();
    }
    public String toOtherCapitalizedString() {
        return switch(this) {
            case DAY -> "Daily";
            case MONTH -> "Monthly";
            case YEAR -> "Yearly";
        };
    }
}
