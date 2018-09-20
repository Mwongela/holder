package org.busaracenter.es;

import com.orm.SugarApp;

import org.busaracenter.es.model.Contribution;
import org.busaracenter.es.model.Goal;

public class App extends SugarApp {
    public static long currentGoalId = -1;
    public static double amount = 0;
    public static String contributionVehicle = "";

    public static void saveContribution() {
        if (currentGoalId != -1){

            if (amount == 0) return;

            Goal goal = Goal.findById(Goal.class, currentGoalId);
            Contribution contribution = new Contribution();
            contribution.setAmount(amount);
            contribution.setContributionVehicle(contributionVehicle);
            contribution.setTimestamp(System.currentTimeMillis());
            contribution.setGoal(goal);

            contribution.save();

            amount = 0;
            contributionVehicle = "";
        }
    }
}