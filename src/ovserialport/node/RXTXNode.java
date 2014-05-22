package ovserialport.node;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gui.components.nodes.InNode;
import gui.components.nodes.OutNode;
import gui.components.ovnode.OVNodeComponent;
import gui.constants.ComponentSettings;
import gui.enums.EditorMode;
import gui.interfaces.OVContainer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.w3c.dom.Element;

import core.Setting;
import core.SlotInterface;
import core.SlotListener;
import core.Value;
import core.ValueDescriptor;
import core.ValueType;

public class RXTXNode extends OVNodeComponent implements SlotListener,
		SerialPortEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 39708097831697706L;
	private static final String _input = "Input";
	private static final String _output = "Output";
	private static final String _port = "Port";
	private ArrayList<CommPortIdentifier> ports_ = new ArrayList<>();
	private OutNode output_;
	private SerialPort port_;
	private OutputStream outStream_;
	private InputStream inStream_;
	private boolean connected_ = false;

	public RXTXNode(OVContainer father) {
		super(father);
		InNode n = addInput(_input, ValueType.INTEGER);
		n.addListener(this);
		output_ = addOutput(_output, ValueType.INTEGER);
		Setting s = new Setting(_port, "port",this);
		addBothSetting(ComponentSettings.SpecificCategory, s);
		initports();
	}

	public RXTXNode(Element e, OVContainer father) {
		super(e, father);
		for (InNode n : inputs_) {
			if (n.getLabel().equals(_input)) {
				n.addListener(this);
			}
		}
		for (OutNode n : outputs_) {
			if (n.getLabel().equals(_output)) {
				output_ = n;
			}
		}
		initports();
	}

	private void initports() {
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		ports_.clear();
		ValueDescriptor desc = getNodeSetting(_port).getValue().getDescriptor();
		ArrayList<String> names = new ArrayList<>();
		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports
					.nextElement();
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ports_.add(curPort);
				names.add(curPort.getName());
				desc.addPossibility(new Value(curPort.getName()));
			}
		}
		String sel = getNodeSetting(_port).getValue().getString();
		if (!names.contains(sel) && names.size() > 0) {
			getNodeSetting(_port).setValue(names.get(0));
		}
	}

	private CommPortIdentifier getSelectedPort() {
		String sel = getNodeSetting(_port).getValue().getString();
		for (CommPortIdentifier port : ports_) {
			if (port.getName().equals(sel)) {
				return port;
			}
		}
		return null;
	}

	private void connect() {
		CommPortIdentifier port = getSelectedPort();
		if (port != null) {
			CommPort commPort = null;
			try {
				commPort = port.open(port.getName(), 1000);
				port_ = (SerialPort) commPort;
				outStream_ = port_.getOutputStream();
				inStream_ = port_.getInputStream();
				initListeners();
				connected_ = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void deconnect() {
		if (connected_) {
			try {
				outStream_.close();
				inStream_.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			port_.removeEventListener();
			port_.close();
			connected_ = false;
		}
	}

	private void initListeners() {

		try {
			port_.addEventListener(this);
			port_.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setMode(EditorMode mode) {
		if (mode.isExec() && !getMode().isExec()) {
			connect();
		} else if (!mode.isExec() && getMode().isExec()) {
			deconnect();
		}
		super.setMode(mode);
	}

	@Override
	public void valueRecived(SlotInterface s, Value v) {
		if (s.getLabel().equals(_input) && connected_) {
			try {
				int val = v.getInt();
				outStream_.write(val);
				outStream_.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void serialEvent(SerialPortEvent e) {
		if (e.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte singleData = (byte) inStream_.read();
				output_.trigger(new Value((int) singleData));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static String getKey() {
		return "RXTXNode";
	}
}
