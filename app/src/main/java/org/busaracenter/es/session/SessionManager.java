package org.busaracenter.es.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {


    private static final String PREF_NAME = "lookielookiehere";

    public static final String KEY_PHONE = "phone";
    public static final String KEY_MONTH = "month";
    public static final String KEY_CONTRIBUTIONS = "contributions";
    public static final String KEY_GOAL_TYPE = "goal_type";
    public static final String KEY_GROUP_TYPE = "group_type";
    public static final String KEY_GOAL_NOTE = "goal_note";
    public static final String KEY_NEXT_NOTIFICATION_TIME = "next_notification_time";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setPhone(String phone) {
        editor.putString(KEY_PHONE, phone);
        editor.commit();
    }

    public String getPhone() {
        return pref.getString(KEY_PHONE, "");
    }

    public void setMonth(String month) {
        editor.putString(KEY_MONTH, month);
        editor.commit();
    }

    public String getMonth() {
        return pref.getString(KEY_MONTH, "");
    }

    public void setContributions(String contributions) {
        editor.putString(KEY_CONTRIBUTIONS, contributions);
        editor.commit();
    }

    public String getContributions() {
        return pref.getString(KEY_CONTRIBUTIONS, "");
    }

    public void setGroupType(String groupType) {
        editor.putString(KEY_GROUP_TYPE, groupType);
        editor.commit();
    }

    public String getGroupType() {
        return pref.getString(KEY_GROUP_TYPE, "");
    }

    public void setGoalType(String goalType) {
        editor.putString(KEY_GOAL_TYPE, goalType);
        editor.commit();
    }

    public String getGoalType() {
        return pref.getString(KEY_GOAL_TYPE, "");
    }

    public String getGoalNote() {
        return pref.getString(KEY_GOAL_NOTE, "");
    }

    public void setGoalNote(String goalNote) {
        editor.putString(KEY_GOAL_NOTE, goalNote);
        editor.commit();
    }

    public long getNextNotificationTime() {
        return pref.getLong(KEY_NEXT_NOTIFICATION_TIME, System.currentTimeMillis() - 1);
    }

    public void setNextNotificationTime(long nextNotificationTime) {
        editor.putLong(KEY_NEXT_NOTIFICATION_TIME, nextNotificationTime);
        editor.commit();
    }

    public boolean isSessionValid() {
        String phone = getPhone();
        String month = getMonth();
        String goalType = getGoalType();
        String groupType = getGroupType();

        return !phone.equals("") && !month.equals("") && !groupType.equals("") && !goalType.equals("");
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}