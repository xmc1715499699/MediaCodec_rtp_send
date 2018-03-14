package com.android.screenrecorder.rtp;

import com.android.screenrecorder.rtp.RtpAacStream;
import com.android.screenrecorder.rtp.RtpAvcStream;
import com.android.screenrecorder.rtp.RtpUdp;

import java.io.IOException;

/**
 * Created by xmc on 2017/6/27.
 * com.android.screenrecorder.record
 */
public class RtpSenderWrapper {

    private RtpUdp mRtpUdp;
    private RtpAacStream mRtpAacStream;
    private RtpAvcStream mRtpAvcStream;

    public RtpSenderWrapper(String ip,int port,boolean broadcast) {
        mRtpUdp = new RtpUdp(ip,port,broadcast);

        if (mRtpUdp != null){
            mRtpAvcStream = new RtpAvcStream(mRtpUdp);
            mRtpAacStream = new RtpAacStream(44100,mRtpUdp);
        }
    }


    public void sendAvcPacket(final byte[] data,final int offset, final int size,long timeUs){
        if (mRtpAvcStream != null){
            try {
                mRtpAvcStream.addPacket(data,offset,size,timeUs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAacPacket(final byte[] data,final int offset, final int size,long timeUs){
        if (mRtpAacStream != null){
            try {
                mRtpAacStream.addAU(data,offset,size,timeUs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //音视频同时关闭
    public void close(){
        if (mRtpUdp != null){
            mRtpUdp.close();
        }
    }
}
