package com.dnieadmin.gui;

import java.util.List;

import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem;

	/**
	 * Interface for radial menu item data.
	 */
	public interface MyRadialMenuInterface {
		public String getName();
		public String getLabel();
		public int getIcon();
		public int getIconSelected();
		public List<MyRadialMenuItem> getChildren();
		public void menuActiviated();
	}