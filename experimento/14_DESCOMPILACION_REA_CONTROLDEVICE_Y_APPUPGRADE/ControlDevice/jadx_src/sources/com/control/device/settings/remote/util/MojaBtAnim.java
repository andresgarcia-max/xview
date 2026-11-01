package com.control.device.settings.remote.util;

import android.app.Activity;
import android.widget.VideoView;
import com.control.device.R;
import com.control.device.settings.remote.util.VideoAnimController;
import com.control.device.settings.remote.util.VideoUtils;
import com.control.device.util.DebugUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class MojaBtAnim extends VideoAnimController {
    private static String curState = "";
    public static final Map<String, List<VideoUtils.Resource>> resListMap;

    static {
        HashMap hashMap = new HashMap();
        resListMap = hashMap;
        ArrayList arrayList = new ArrayList();
        arrayList.add(new VideoUtils.Resource(R.raw.btpair_01_searching, -1));
        hashMap.put("prepaired-searching", arrayList);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new VideoUtils.Resource(R.raw.btpair_00_continue, -1));
        hashMap.put("await", arrayList2);
        ArrayList arrayList3 = new ArrayList();
        arrayList3.add(new VideoUtils.Resource(R.raw.btpair_01_searching, 3));
        arrayList3.add(new VideoUtils.Resource(R.raw.btpair_02_tran_01, 1));
        arrayList3.add(new VideoUtils.Resource(R.raw.btpair_03_instruction, -1));
        hashMap.put("bfScan", arrayList3);
        ArrayList arrayList4 = new ArrayList();
        arrayList4.add(new VideoUtils.Resource(R.raw.btpair_02_tran_01, 1));
        arrayList4.add(new VideoUtils.Resource(R.raw.btpair_03_instruction, -1));
        hashMap.put("onScan", arrayList4);
        ArrayList arrayList5 = new ArrayList();
        arrayList5.add(new VideoUtils.Resource(R.raw.btpair_03_instruction, -1));
        hashMap.put("reScan", arrayList5);
        ArrayList arrayList6 = new ArrayList();
        arrayList6.add(new VideoUtils.Resource(R.raw.btpair_04_tran_02, 1));
        arrayList6.add(new VideoUtils.Resource(R.raw.btpair_05_connecting, -1));
        hashMap.put("onBond", arrayList6);
        ArrayList arrayList7 = new ArrayList();
        arrayList7.add(new VideoUtils.Resource(R.raw.btpair_06_success, 1));
        hashMap.put("done", arrayList7);
        ArrayList arrayList8 = new ArrayList();
        arrayList8.add(new VideoUtils.Resource(R.raw.btpair_07_error, 1));
        hashMap.put("error", arrayList8);
    }

    public static class Controller extends VideoAnimController.Controller {
        private final Activity activity;
        private int curIndex = 0;
        private List<VideoUtils.Resource> curList;
        private final VideoView vv;

        class VCallback implements VideoUtils.videoPlayCallback {
            VCallback() {
            }

            @Override // com.control.device.settings.remote.util.VideoUtils.videoPlayCallback
            public void onCompletion() {
                Controller controller = Controller.this;
                controller.toNext(controller.curList);
            }
        }

        @Override // com.control.device.settings.remote.util.VideoAnimController.Controller
        public void changeState(String str) {
            DebugUtil.dTAGForClassName("vv state: " + this.vv.isPlaying());
            if (str.equals(MojaBtAnim.curState)) {
                DebugUtil.dTAGForClassName("same state no action");
                return;
            }
            DebugUtil.dTAGForClassName("cur state is " + MojaBtAnim.curState + " change state to : " + str);
            String unused = MojaBtAnim.curState = str;
            this.vv.stopPlayback();
            List<VideoUtils.Resource> list = MojaBtAnim.resListMap.get(str);
            this.curList = list;
            this.curIndex = 0;
            if (list != null) {
                startPlay(list);
            }
        }

        public boolean toNext(List<VideoUtils.Resource> list) {
            if (list == null || this.curIndex + 1 == list.size()) {
                return false;
            }
            this.curIndex++;
            startPlay(list);
            return true;
        }

        public void startPlay(List<VideoUtils.Resource> list) {
            VideoUtils.Resource resource = list.get(this.curIndex);
            VideoUtils.playVideoWithCallback(this.vv, VideoUtils.video2Uri(this.activity, resource.resId), resource.count, new VCallback());
        }

        public Controller(Activity activity, VideoView videoView) {
            this.activity = activity;
            this.vv = videoView;
            videoView.requestFocus();
        }
    }
}
