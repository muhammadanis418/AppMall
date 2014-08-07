package cn.koolcloud.ipos.appstore.download.multithread;

public class FinishEvent implements Event {

	@Override
	public String getEvent() {
		return Event.finishEvent;
	}

}
