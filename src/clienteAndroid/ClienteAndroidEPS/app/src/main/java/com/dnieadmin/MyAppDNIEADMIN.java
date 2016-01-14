package com.dnieadmin;

import de.tsenger.androsmex.data.CANSpecDO;

import android.app.Application;


public class MyAppDNIEADMIN extends Application {
	
	public boolean m_started;
	private String m_url;
	private CANSpecDO selectedCAN;
	
	public void setCAN(CANSpecDO can)
	{
		selectedCAN = can;
	}
	
	public CANSpecDO getCAN()
	{
		return selectedCAN;
	}

	public void setUrl(String url)
	{
		m_url = url;
	}
	
	public String getUrl()
	{
		return m_url;
	}
	
	public boolean isStarted()
	{
		return m_started;
	}
	
	public void setStarted(boolean state)
	{
		m_started = state;
	}
	  
}