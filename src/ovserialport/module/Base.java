package ovserialport.module;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ovserialport.node.RXTXNode;
import core.module.BaseModule;

public class Base extends BaseModule {

	public Base() {
		super("OV Serial Port", "0.0");
	}

	@Override
	public ArrayList<JMenu> getNodeMenus() {
		ArrayList<JMenu> menus = new ArrayList<>();
		JMenu m=new JMenu("Serial Port");
		JMenuItem i=new JMenuItem(RXTXNode.getKey());
		i.setActionCommand(i.getText());
		m.add(i);
		menus.add(m);
		return menus;
	}

	@Override
	public ArrayList<JMenu> getGuiMenus() {
		ArrayList<JMenu> menus = new ArrayList<>();
		return menus;
	}

	@Override
	public HashMap<String, Class<?>> getComponents() {
		HashMap<String, Class<?>> classes = new HashMap<>();
		classes.put(RXTXNode.getKey(), RXTXNode.class);
		return classes;
	}

}
