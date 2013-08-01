package com.appdelegates.solnetwork;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


public class IPAddressHelper {

	public static final String getFunctionForIp(String ipString){
		
		String[] parts = ipString.split("\\.");
		
		if ( parts[0].equals("192") && parts[1].equals("168") && parts[2].equals("1") ){
			
			// We are on the right subnet
			int ip = Integer.parseInt(parts[3]);
			
			switch (ip){
			
			case 50:
				return "System Master";
				
			case 51:
				return "Login Tablet";
				
			case 52:
				return "Leaderboard";
				
			case 100:
				return "Scoreboard A";
							
			case 110:
				return "Unit A. Row 1, Column 1.";
			case 111:
				return "Unit A. Row 1, Column 2.";
			case 112:
				return "Unit A. Row 1, Column 3.";
				
			case 113:
				return "Unit A. Row 2, Column 1.";
			case 114:
				return "Unit A. Row 2, Column 2.";
			case 115:
				return "Unit A. Row 2, Column 3.";
				
			case 116:
				return "Unit A. Row 3, Column 1.";
			case 117:
				return "Unit A. Row 3, Column 2.";
			case 118:
				return "Unit A. Row 3, Column 3.";
				
			case 119:
				return "Unit A. Row 4, Column 1.";
			case 120:
				return "Unit A. Row 4, Column 2.";
			case 121:
				return "Unit A. Row 4, Column 3.";
				
			case 122:
				return "Unit A. Row 5, Column 1.";
			case 123:
				return "Unit A. Row 5, Column 2.";
			case 124:
				return "Unit A. Row 5, Column 3.";
				
			case 200:
				return "Scoreboard B";	
				
			case 210:
				return "Unit B. Row 1, Column 1.";
			case 211:
				return "Unit B. Row 1, Column 2.";
			case 212:
				return "Unit B. Row 1, Column 3.";
				
			case 213:
				return "Unit B. Row 2, Column 1.";
			case 214:
				return "Unit B. Row 2, Column 2.";
			case 215:
				return "Unit B. Row 2, Column 3.";
				
			case 216:
				return "Unit B. Row 3, Column 1.";
			case 217:
				return "Unit B. Row 3, Column 2.";
			case 218:
				return "Unit B. Row 3, Column 3.";
				
			case 219:
				return "Unit B. Row 4, Column 1.";
			case 220:
				return "Unit B. Row 4, Column 2.";
			case 221:
				return "Unit B. Row 4, Column 3.";
				
			case 222:
				return "Unit B. Row 5, Column 1.";
			case 223:
				return "Unit B. Row 5, Column 2.";
			case 224:
				return "Unit B. Row 5, Column 3.";
			
			default: 
				return "Right subnet, unused IP.";
			}
			
		} else {
			return "Wrong Subnet. Set Proper IP.";
		}
		
	}
	
		
	public static final String getLocalIpAddress(Context context) {
		
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));
		
		return ipString;
		
    }
	
	public static final String getMasterForIPString(String ipString){
		
		String[] parts = ipString.split("\\.");
		int last = Integer.parseInt(parts[3]);
		
		if (last<200)
			return "192.168.1.100";
		else
			return "192.168.1.200";			
	}
	
	public static final InetAddress getMasterForIP(String ipString) throws UnknownHostException{
		
		String[] parts = ipString.split("\\.");
		int last = Integer.parseInt(parts[3]);
		
		if (last<200)
			return InetAddress.getByName("192.168.1.100");
		else
			return InetAddress.getByName("192.168.1.200");		
	}
	
	public static final int getShortIP(InetAddress ipAddr){
		String ip = ipAddr.getHostAddress();
		String [] parts = ip.split("\\.");
		int shortIP = Integer.parseInt( parts[3]  );
		return shortIP;
	}
	
	public static final String getShortIPString(String ip){
		String [] parts = ip.split("\\.");
		return parts[3];
	}
}
