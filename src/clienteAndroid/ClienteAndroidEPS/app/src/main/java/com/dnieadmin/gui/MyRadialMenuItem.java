package com.dnieadmin.gui;

import java.util.List;

/**
 * This class handles the menu item creation.
 * 
 * @author Jason Valestin (valestin@gmail.com )
 * @author Arindam Nath (strider2023@gmail.com)
 */
public class MyRadialMenuItem implements MyRadialMenuInterface {

	private String menuName 	 = "Empty";
	private String menuLabel 	 = null;
	private int menuIcon 		 = 0;
	private int menuIconSelected = 0;
	private List<MyRadialMenuItem> menuChildren = null;
	private RadialMenuItemClickListener menuListener = null;
	
	/**
	 * Creates an instance of the RadialMenuItem.
	 * @param name - (String) If there is no name to be assigned pass null. (Name is mostly used for debugging)
	 * @param displayName - (String) If there is no display name to be assigned pass null.
	 */
	public MyRadialMenuItem(String name, String displayName) {
		if(name != null)
			this.menuName = name;
		this.menuLabel = displayName;
	}
	
	/**
	 * Set menu item icon.
	 * @param displayIcon - (int) Icon resource ID.
	 * <strong>secondChildItem.setDisplayIcon(R.drawable.ic_launcher);</strong>
	 */
	public void setDisplayIcon(int displayIcon) {
		this.menuIcon = displayIcon;
	}
	
	/**
	 * Set menu item icon when selected
	 * @param displayIcon - (int) Icon resource ID.
	 * <strong>secondChildItem.setDisplayIconSelected(R.drawable.ic_launcher);</strong>
	 */
	public void setDisplayIconSelected(int displayIcon) {
		this.menuIconSelected = displayIcon;
	}
	
	/**
	 * Set the on menu item click event.
	 * @param listener
	 */
	public void setOnMenuItemPressed(RadialMenuItemClickListener listener) {
		menuListener = listener;
	}
	
	/**
	 * Set the menu child items.
	 * @param childItems - Pass the list of child items.
	 */
	public void setMenuChildren(List<MyRadialMenuItem> childItems) {
		this.menuChildren = childItems;
	}
	
	@Override
	public String getName() {
		return menuName;
	}

	@Override
	public String getLabel() {
		return menuLabel;
	}

	@Override
	public int getIcon() {
		return menuIcon;
	}
	
	@Override
	public int getIconSelected() {
		return menuIconSelected;
	}

	@Override
	public List<MyRadialMenuItem> getChildren() {
		return menuChildren;
	}

	@Override
	public void menuActiviated() {
		if(menuListener != null)
			menuListener.execute();
	}
	
	public interface RadialMenuItemClickListener {
		public void execute();
	}
}
