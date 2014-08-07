package cn.koolcloud.ipos.appstore.download.multithread;

public interface Event {

    public static final String startEvent = "Start_Event";
    public static final String pauseEvent = "Pause_Event";
    public static final String cancelEvent = "Cancel_Event";
    public static final String finishEvent = "Finish_Event";

    public abstract String getEvent();
}