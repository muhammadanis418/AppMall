package cn.koolcloud.ipos.appstore.download.multithread;

public class CancelEvent implements Event {

	@Override
	public String getEvent() {
		return Event.cancelEvent;
	}

}
