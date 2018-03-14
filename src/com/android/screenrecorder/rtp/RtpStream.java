package com.android.screenrecorder.rtp;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RtpStream {

	private static final String TAG = "RtpStream";
	private int payloadType;
	private int sampleRate;
	private RtpSocket socket;
	private short sequenceNumber;
	private long timeold;


	public RtpStream(int pt, int sampleRate, RtpSocket socket){
		this.payloadType = pt;
		this.sampleRate = sampleRate;
		this.socket = socket;
	}	

	public void addPacket(byte[] data, int offset, int size, long timeUs) throws IOException{
		addPacket(null, data, offset, size, timeUs);
	}

	public void addPacket(byte[] prefixData, byte[] data, int offset, int size, long timeUs) throws IOException{
	
		/*
		RTP packet header
		Bit offset[b]	0-1	2	3	4-7	8	9-15	16-31
		0			Version	P	X	CC	M	PT	Sequence Number  31
		32			Timestamp									 63
		64			SSRC identifier								 95
		*/

		ByteBuffer buffer = ByteBuffer.allocate(500000);
		buffer.put((byte)(2 << 6));
		buffer.put((byte)(payloadType));
		buffer.putShort(sequenceNumber++);
		buffer.putInt((int)(timeUs));
		buffer.putInt(12345678);

		buffer.putInt(size);

		if(prefixData != null)
			buffer.put(prefixData);

		buffer.put(data, offset, size);
		
		sendPacket(buffer, buffer.position());

	}
	
	protected void sendPacket(ByteBuffer buffer, int size) throws IOException{
		socket.sendPacket(buffer.array(), 0, size);
		buffer.clear();
	}
}
