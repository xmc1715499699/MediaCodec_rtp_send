package com.android.screenrecorder.rtp;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RtpAacStream extends RtpStream {

	public RtpAacStream(int sampleRate, RtpSocket socket){
		super(97, sampleRate, socket);
	}
	
	public void addAU(ByteBuffer buf, int size, long timeUs) throws IOException{
		byte[] data = new byte[size];
		buf.get(data);
		
		addAU(data, 0, size, timeUs);
	}

	public void addAU(byte[] data, int offset, int size, long timeUs) throws IOException{

		int auHeadersLength = 1 * 2 * 8;
		int auHeader = size << 3;
		
		ByteBuffer payload = ByteBuffer.allocate(2 + 2 + size);
		payload.putShort((short)auHeadersLength);
		payload.putShort((short)auHeader);
		
		payload.put(data, offset, size);

		addPacket(payload.array(), 0, payload.position(), timeUs);		
	}

}
