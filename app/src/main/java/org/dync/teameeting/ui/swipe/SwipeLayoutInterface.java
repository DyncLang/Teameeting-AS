package org.dync.teameeting.ui.swipe;

import org.dync.teameeting.ui.swipe.SwipeLayout.Status;

public interface SwipeLayoutInterface {

	Status getCurrentStatus();
	
	void close();
	
	void open();
}
