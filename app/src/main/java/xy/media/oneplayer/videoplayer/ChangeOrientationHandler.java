package xy.media.oneplayer.videoplayer;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

public class ChangeOrientationHandler extends Handler {

	private VideoPlayerActivity activity;

	public ChangeOrientationHandler(VideoPlayerActivity ac) {
		super();
		activity = ac;
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (msg.what == 888) {
			int orientation = msg.arg1;
			int contentOritation = activity.getmOrientationByVideoContent();
			if(contentOritation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
				if (orientation>45&&orientation<135) {
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}else if (orientation>225&&orientation<315){
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			} else {
				if (orientation>135&&orientation<225){
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}else if ((orientation>315&&orientation<360)||(orientation>0&&orientation<45)){
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		}
		
		super.handleMessage(msg);        		
	}
	
}
