package com.vnbear.hktestdemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Sjrs08SurfaceView nowSjrs08SurfaceView;   //SurfaceView对象，用来显示视频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nowSjrs08SurfaceView = (Sjrs08SurfaceView) findViewById(R.id.video_0);
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!nowSjrs08SurfaceView.playFlag) {   //如果没有在播放的话
            MonitorCameraInfo cameraInfo = new MonitorCameraInfo();

            cameraInfo.serverip = "192.168.0.100";
            cameraInfo.serverport = 8000;
            cameraInfo.username = "ceshiadmin";
            cameraInfo.userpwd = "123456";
            cameraInfo.channel = 1;
            cameraInfo.describe = "测试点";

            nowSjrs08SurfaceView.setMonitorInfo(cameraInfo);
            nowSjrs08SurfaceView.startPlay();   //开始实时预览
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nowSjrs08SurfaceView.playFlag) {
            nowSjrs08SurfaceView.stopPlay();   //停止实时预览
        }

    }


}
