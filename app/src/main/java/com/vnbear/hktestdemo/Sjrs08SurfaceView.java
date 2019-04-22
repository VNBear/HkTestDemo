package com.vnbear.hktestdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_CLIENTINFO;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.RealPlayCallBack;

import org.MediaPlayer.PlayM4.Player;

public class Sjrs08SurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private HCNetSDK videoCtr;    //网络库sdk
    private Player myPlayer = null;  //播放库sdk
    private int playPort = -1;   //播放端口
    public boolean playFlag = false;   //播放标志
    private int userId = -1;   //登录帐号id
    private MonitorCameraInfo cameraInfo = null;   //监控点信息

    private SurfaceHolder holder = null;

    public Sjrs08SurfaceView(Context paramContext) {
        super(paramContext);
        initSurfaceView();

    }

    public Sjrs08SurfaceView(Context paramContext,
                             AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initSurfaceView();
    }

    public Sjrs08SurfaceView(Context paramContext,
                             AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet);
        initSurfaceView();
    }

    private void initSurfaceView() {

        getHolder().addCallback(this);

    }

    public boolean onDown(MotionEvent paramMotionEvent) {
        return false;
    }

    public boolean onFling(MotionEvent paramMotionEvent1,
                           MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        return false;
    }

    public void onLongPress(MotionEvent paramMotionEvent) {
    }

    public boolean onScroll(MotionEvent paramMotionEvent1,
                            MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        return false;
    }

    public void onShowPress(MotionEvent paramMotionEvent) {
    }

    public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
        return false;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder = this.getHolder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void setMonitorInfo(MonitorCameraInfo setMonitorInfo) {
        this.cameraInfo = setMonitorInfo;
    }

    /**
     * 时间如水2013-7-10
     * 功能：暂定播放
     * flag 1/暂停 0/恢复
     */
    public void pausePaly(int flag) {
        myPlayer.pause(playPort, flag);
    }

    public void stopPlay() {

        try {
            playFlag = false;
            videoCtr.NET_DVR_StopRealPlay(playPort);
            videoCtr.NET_DVR_Logout_V30(userId);

            userId = -1;

            videoCtr.NET_DVR_Cleanup();

            if (myPlayer != null) {
                myPlayer.stop(playPort);
                myPlayer.closeStream(playPort);
                myPlayer.freePort(playPort);

                playPort = -1;

                destroyDrawingCache();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    /**
     * 时间如水2013-7-10
     * 功能：开始实时预览
     */
    public void startPlay() {

        try {

            // 实例化播放API
            myPlayer = Player.getInstance();

            // 实例化海康威视android sdk
            videoCtr = new HCNetSDK();

            videoCtr.NET_DVR_Init();  //初始化海康威视android sdk

            videoCtr.NET_DVR_SetExceptionCallBack(mExceptionCallBack);  //设置错误回掉函数

            // 设置连接超时时长
            videoCtr.NET_DVR_SetConnectTime(60000);

            playPort = myPlayer.getPort();  //获取空闲播放端口

            NET_DVR_DEVICEINFO_V30 deviceInfo = new NET_DVR_DEVICEINFO_V30();
            // 登录服务器
            userId = videoCtr.NET_DVR_Login_V30(cameraInfo.serverip,
                    cameraInfo.serverport, cameraInfo.username,
                    cameraInfo.userpwd, deviceInfo);

            System.out.println("下面是设备信息************************");
            System.out.println("通道开始=" + deviceInfo.byStartChan);
            System.out.println("通道个数=" + deviceInfo.byChanNum);
            System.out.println("设备类型=" + deviceInfo.byDVRType);
            System.out.println("ip通道个数=" + deviceInfo.byIPChanNum);

            byte[] sbbyte = deviceInfo.sSerialNumber;
            String sNo = "";
            for (int i = 0; i < sbbyte.length; i++) {
                sNo += String.valueOf(sbbyte[i]);
            }

            System.out.println("设备序列号=" + sNo);

            System.out.println("************************");

            NET_DVR_CLIENTINFO clientInfo = new NET_DVR_CLIENTINFO();

            clientInfo.lChannel = cameraInfo.channel;
            clientInfo.lLinkMode = 0x80000000; // 子码流，保证图像实时性
            clientInfo.sMultiCastIP = null;

            int playFlag = videoCtr.NET_DVR_RealPlay_V30(userId, clientInfo,
                    mRealPlayCallBack, false);
            System.out.println("playFlag=" + playFlag);
            System.out.println("GetLastError="
                    + videoCtr.NET_DVR_GetLastError());

        } catch (Exception e) {
            e.printStackTrace();

            stopPlay();
        }

    }

    private ExceptionCallBack mExceptionCallBack = new ExceptionCallBack() {

        @Override
        public void fExceptionCallBack(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            System.out.println("异常回掉函数运行！");

        }
    };

    private RealPlayCallBack mRealPlayCallBack = new RealPlayCallBack() {

        @Override
        public void fRealDataCallBack(int lRealHandle, int dataType,
                                      byte[] paramArrayOfByte, int byteLen) {
            // TODO Auto-generated method stub

            // System.out.println("playFlag=" + playFlag + "，dataType=" +
            // dataType + "，大小=" + byteLen + "字节，playPort=" + playPort);

            if (playPort == -1)
                return;

            switch (dataType) {
                case 1: // 头数据

                    if (myPlayer.openStream(playPort, paramArrayOfByte, byteLen,
                            1024 * 1024)) {
                        if (myPlayer.setStreamOpenMode(playPort, 1)) {
                            if (myPlayer.play(playPort, holder)) {
                                playFlag = true;
                            } else {
                                playError(3);
                            }
                        } else {
                            playError(2);
                        }
                    } else {
                        playError(1);
                    }

                    break;
                case 4:

                    if (playFlag
                            && myPlayer.inputData(playPort, paramArrayOfByte,
                            byteLen)) {
                        playFlag = true;
                    } else {
                        playError(4);
                        playFlag = false;
                    }

            }

        }

    };

    private void playError(int step) {

        switch (step) {
            case 1:
                System.out.println("openStream error,step=" + step);
                break;
            case 2:
                System.out.println("setStreamOpenMode error,step=" + step);
                break;
            case 3:
                System.out.println("play error,step=" + step);
                break;
            case 4:
                System.out.println("inputData error,step=" + step);
                break;
        }

        stopPlay();
    }

}
