import ithakimodem.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Arrays;

public class src 
{
	Modem modem;
	int k;

	public src(String echoCode, String imageCode, String errorImageCode, String gpsCode, String ackCode, String nackCode)
	{
		this.modem = new Modem();
		this.modem.setSpeed(80000);
		this.modem.setTimeout(1000);
		this.modem.open("ithaki");		
//		packetResponses(echoCode + "\r");	
//		image(imageCode +"\r", "Image");
//		image(errorImageCode + "\r", "ErrorImage");
//		gps(gpsCode +"R=1001090\r");
		arq(ackCode +"\r", nackCode +"\r");
		this.modem.close();
	}
	
	private boolean packet(String echoCode,boolean arqError)
	{	
		String echoPacket = "";
		
		this.modem.write(echoCode.getBytes());	
		for(;;) 
		{
				try
				{
					k = this.modem.read();
					
					if (echoPacket.indexOf("PSTOP") > 0) {  break;  }
					if (k == -1) 
					{  
						System.out.println("connection lost");
						break;
					}
					
					echoPacket = echoPacket + (char) k;
					
				} catch (Exception x) {
					System.out.println(x.toString());
					break;
				}
		}
		
		if(arqError)
		{
			String temp1 = echoPacket.split("<")[1];
			String xSequence = temp1.split(">")[0]; 
			String temp2 = echoPacket.split("PSTART")[1];
			String fcs = temp2.split(" ")[5];
			
			int check = 0;
			for(int j = 0; j < xSequence.length(); j++) 
			{
			check = (char) check ^ xSequence.charAt(j);
			}
		    if (check == Integer.valueOf(fcs)) 
			{return true;}
			else 
			{return false;}		
		}
		else {return true;}
	}
	
	private  void  packetResponses(String  echoCode) 
	{ 
		ArrayList<Integer>  packetDelays  =  new  ArrayList<Integer>(); 
		int  clk  =  0;
		
		for  (int  i  =  0;  clk  <  300000;  i++) 
		{ 
	           long  clkPacketResponse  =  0;
	           clkPacketResponse  =  System.currentTimeMillis(); 
               packet(echoCode,false);
               clkPacketResponse  =  System.currentTimeMillis()  -  clkPacketResponse;

		   packetDelays.add((int)  clkPacketResponse );

			if  (i  !=  0)  
			{	clk  = clk +  packetDelays.get(i);	}
		}
		System.out.println("Table of server sent packet response times : ");
		System.out.println();
		System.out.println(Arrays.toString(packetDelays.toArray()));
     }

	private void image(String imageCode, String imageName)
	{
		String modemResponse = "";
		ArrayList<Byte> imageBytesList = new ArrayList<Byte>(); 
		imageBytesList.add((byte) 255);
		boolean ready = false;
		
		this.modem.write(imageCode.getBytes());
		for(;;) {
			try {
					k = this.modem.read(); 
					if (k == -1) 
					{
					System.out.println("connection lost"); 
					break;
					}
					modemResponse = modemResponse + (char) k;
					
					if (ready == false)
					{
						if (modemResponse.indexOf("" + (char) 255 + (char) 216) > 0)
						{ready = true;}
					}
					
					if(ready) 
					{imageBytesList.add((byte) k);}
					
					if (modemResponse.indexOf("" + (char) 255 + (char) 217) > 0)
					{
						byte[] imageBytesArray = new byte[imageBytesList.size()];
						
						for (int i = 0; i < imageBytesList.size(); i++) 
						{imageBytesArray[i] = imageBytesList.get(i);}
						
						ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytesArray); 
						BufferedImage buffImage = ImageIO.read(inputStream); 
						File file = new File("C:\\Users\\user\\Desktop\\AUTh\\6TH SEMESTER\\Δικτυα Υπολογιστων Ι\\Εργασια 2020\\" + imageName + ".jpeg");
						ImageIO.write(buffImage, "jpeg", file);
						System.out.println("Image saved to path");
						break;
					}	
			} catch (Exception x) {
				System.out.println(x);
				break;
			}
		}
	}
	
	private void gps(String gpsCode) 
	{
		String gpsSentence = "";
		this.modem.write(gpsCode.getBytes());
		for (;;) {
			try
			{
				k = this.modem.read(); 
				if (k == -1) 
				{
				System.out.println("connection lost"); 
				break;
				}
				if (gpsSentence.indexOf("STOP ITHAKI GPS TRACKING") > 0) {break;}
				
				gpsSentence = gpsSentence + (char) k;
			} 
			catch (Exception x) {
				System.out.print(x.toString());
			}	
		}
		
		String[] allMarkers = gpsSentence.split("GPGGA");
	
		String[] lat = new String[5];
		String[] longit = new String[5];
		int nonIntLat = 0;
		int nonIntLongit = 0;
		
		for (int selectedMarker = 1, j = 0; selectedMarker < allMarkers.length; selectedMarker = selectedMarker + 20, j++)
		{
			lat[j] = allMarkers[selectedMarker].split(",")[2];
			nonIntLat = Math.round(Float.valueOf(lat[j]) % 1 * 60);
			lat[j] = String.valueOf(Float.valueOf(lat[j]).intValue() * 100 + nonIntLat );
			
			longit[j] = allMarkers[selectedMarker].split(",")[4];
			nonIntLongit = Math.round(Float.valueOf(allMarkers[selectedMarker].split(",")[2]) % 1 * 60);
			longit[j] = String.valueOf(Float.valueOf(longit[j]).intValue() * 100 + nonIntLongit);
		}
		
		String gpsImageCode = "" + gpsCode.split("R")[0];
		for (int l = 0; l < 5; l++) 
		{
			gpsImageCode = gpsImageCode + "T=" + longit[l] + lat[l];
		}
		gpsImageCode = gpsImageCode + "\r";
			
		String modemResponse = "";
		ArrayList<Byte> gpsImageBytesList = new ArrayList<Byte>(); 
		gpsImageBytesList.add((byte) 255);
		
		boolean ready = false;
		this.modem.write(gpsImageCode.getBytes());
		for(;;)
		{
			try 
			{
				k = this.modem.read(); 
				
				if (k == -1) 
			    {
				    System.out.println("connection lost"); 
				    break;
			    }
			
				    modemResponse += (char) k;
				
				if (ready == false) 			
			    {	
				    if (modemResponse.indexOf("" + (char) 255 + (char) 216) > 0) 
				    {ready = true;}
			    }
				if(ready)
				{gpsImageBytesList.add((byte) k);}
				
				if (modemResponse.indexOf("" + (char) 255 + (char) 217) > 0)
				{
		            byte[] gpsImageBytesArray = new byte[gpsImageBytesList.size()];
					
				   	for (int i = 0; i < gpsImageBytesList.size(); i++)
				    {gpsImageBytesArray[i] = gpsImageBytesList.get(i);}
				
				    ByteArrayInputStream inputStream = new ByteArrayInputStream(gpsImageBytesArray);
				    BufferedImage buffImage = ImageIO.read(inputStream);
					File file = new File("C:\\Users\\user\\Desktop\\AUTh\\6TH SEMESTER\\Δικτυα Υπολογιστων Ι\\Εργασια 2020\\gps.jpeg");
				    ImageIO.write(buffImage, "jpeg", file);
				    System.out.println("GPS Image saved to path");
				    break;
				 }
				
			} catch (Exception x) {
				System.out.println(x.toString());
				break;
			}
		}
		
	}

	private void arq(String AckCode, String NackCode) 
	{	
		long clk = System.currentTimeMillis();

		int ack = 0;
		int nack = 0;
		
		ArrayList<Integer> nackRetransmissionNum = new ArrayList<Integer>();
		
		ArrayList<Integer> arqPacketResponses = new ArrayList<Integer>(); 
		
		while ( System.currentTimeMillis() - clk < 300000)
		{		
			long clkPacket = System.currentTimeMillis();
			
			if (packet(AckCode,true) == false) 
			{
				
					for (int t = 1; packet(NackCode,true) == false; t++)
					{
						nack++;
						nackRetransmissionNum.add(t);
					}
			}
			ack++;
			arqPacketResponses.add((int) (System.currentTimeMillis() - clkPacket));
		}
		
		System.out.println("Table of server sent packet response times : "); System.out.println();
		System.out.println(Arrays.toString(arqPacketResponses.toArray())); System.out.println();System.out.println();
		System.out.println("Packets w/o error (Positive Acknowledgement): " + ack + "\nPackets w/ error (Negative Acknowledgement Packets): " + nack);
		
		System.out.println("Number retransmissions in nack packets:");
		System.out.println(Arrays.toString(nackRetransmissionNum.toArray()));
	}	
	
	public  static  void  main(String[]  args)  
	{
	     new  src("E4623","M2044","G2647","P1276","Q5754","R4885");
	}
}