package com.benjaminfaal.ps4remotepkginstaller.ui;

import com.benjaminfaal.ps4remotepkginstaller.Settings;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallPackagesRequest;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Properties;

@CommonsLog
public class SetupUI extends JDialog {
    private JPanel contentPane;

    private JButton btnOk;

    private JButton btnCancel;

    private JComboBox<String> cmbAddresses;

    private JSpinner spnPort;

    private JCheckBox chkEnableServer;

    @Getter
    private final Properties properties = new Properties();

    @Getter
    private boolean canceled;

    public SetupUI() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("Setup");
        try {
            setIconImage(ImageIO.read(new ClassPathResource("icon.png").getInputStream()));
        } catch (IOException e) {
            log.error("Error setting icon: ", e);
        }

        getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener(e -> onOK());

        btnCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        try {
            Settings.load(properties);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings, reverting to default");
        }
        init();
    }

    private void init() {
        String[] addresses = getAddresses();

        String webApplicationType = properties.getProperty("spring.main.web-application-type", WebApplicationType.SERVLET.name());
        chkEnableServer.setSelected(WebApplicationType.SERVLET.name().equals(webApplicationType));
        chkEnableServer.addChangeListener(e -> {
            spnPort.setEnabled(chkEnableServer.isSelected());
            cmbAddresses.setEnabled(chkEnableServer.isSelected());
        });

        cmbAddresses.setModel(new DefaultComboBoxModel<>(addresses));
        if (properties.containsKey("server.address")) {
            cmbAddresses.setSelectedItem(properties.getProperty("server.address"));
        }

        spnPort.setModel(new SpinnerNumberModel(Integer.parseInt(properties.getProperty("server.port", "8080")), 1, 65535, 1));
        spnPort.setEditor(new JSpinner.NumberEditor(spnPort,"#"));
    }

    private String[] getAddresses() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(networkInterface -> {
                        try {
                            return networkInterface.isUp() && !networkInterface.isVirtual() && !networkInterface.isLoopback();
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .map(networkInterface -> Collections.list(networkInterface.getInetAddresses()).stream().filter(inetAddress -> inetAddress instanceof Inet4Address).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .map(InetAddress::getHostAddress)
                    .sorted(Comparator.reverseOrder())
                    .toArray(String[]::new);
        } catch (SocketException e) {
            throw new IllegalStateException("Failed to get addresses: ", e);
        }
    }

    private void onOK() {
        boolean oldEnableServer = properties.getProperty("spring.main.web-application-type", WebApplicationType.NONE.name()).equals(WebApplicationType.SERVLET.name());
        boolean newEnableServer = chkEnableServer.isSelected();

        String oldPort = properties.getProperty("server.port", "8080");
        String oldAddress = properties.getProperty("server.address");

        String newPort = String.valueOf(spnPort.getValue());
        String newAddress = (String) cmbAddresses.getSelectedItem();

        if (properties.getProperty("tasks", "").contains(InstallPackagesRequest.class.getName())) {
            if (!oldPort.equals(newPort) || (StringUtils.hasText(oldAddress) && !oldAddress.equals(newAddress))) {
                String warning = "Keep in mind that existing local PKG tasks wont download anymore when you change the address or port.";
                JOptionPane.showMessageDialog(this, warning, "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }

        properties.setProperty("server.port", newPort);
        properties.setProperty("server.address", newAddress);
        properties.setProperty("spring.main.web-application-type", newEnableServer ? WebApplicationType.SERVLET.name() : WebApplicationType.NONE.name());

        canceled = false;
        dispose();
    }

    private void onCancel() {
        properties.clear();

        canceled = true;
        dispose();
    }

}
