package cn.koolcloud.ipos.appstore.download.multithread;

public class PauseEvent implements Event {

	@Override
	public String getEvent() {
		return Event.pauseEvent;
	}

}
