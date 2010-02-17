package net.sourcewalker.fakegps.gui;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import net.sourcewalker.fakegps.data.IDataModel;
import net.sourcewalker.fakegps.data.ModelChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public class DeviceActions extends ActionsBase {

    private static final Log LOGGER = LogFactory.getLog(DeviceActions.class);

    private DevicePanel devicePanel;

    private Socket deviceSocket = null;

    private PrintStream deviceStream = null;

    public DeviceActions(final IDataModel dataModel) {
        super(dataModel);

        dataModel.addChangeListener(new PositionListener());
    }

    @Action
    public final void connect() {
        if (!isConnected()) {
            int port = devicePanel.getPort();
            try {
                deviceSocket = new Socket("localhost", port);
                deviceStream = new PrintStream(deviceSocket.getOutputStream());
            } catch (IOException e) {
                LOGGER.error("Error connecting: " + e);
                deviceSocket = null;
            }
        }
    }

    @Action
    public final void disconnect() {
        if (isConnected()) {
            try {
                deviceStream.close();
                deviceSocket.close();
            } catch (IOException e) {
                LOGGER.error("Error while closing connection: " + e);
            }
            deviceSocket = null;
        }
    }

    public final void setPanel(final DevicePanel panel) {
        devicePanel = panel;
    }

    public final boolean isConnected() {
        return deviceSocket != null && deviceSocket.isConnected();
    }

    public final void sendNewPosition(final double latitude,
            final double longitude) {
        StringBuilder msg = new StringBuilder();
        msg.append("geo fix ");
        msg.append(longitude);
        msg.append(" ");
        msg.append(latitude);
        System.out.println("Sending: " + msg.toString());

        deviceStream.println(msg.toString());
    }

    private class PositionListener implements ModelChangeListener {

        /*
         * (non-Javadoc)
         * @see net.sourcewalker.fakegps.data.ModelChangeListener#dataChanged()
         */
        @Override
        public void dataChanged() {
            if (isConnected()) {
                GeoPosition pos = getModel().getRoutePosition();
                if (pos != null) {
                    sendNewPosition(pos.getLatitude(), pos.getLongitude());
                }
            }
        }

    }

}
