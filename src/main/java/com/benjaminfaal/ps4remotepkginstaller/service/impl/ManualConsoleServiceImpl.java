package com.benjaminfaal.ps4remotepkginstaller.service.impl;

import com.benjaminfaal.ps4remotecontrol.ddp.model.Status;
import com.benjaminfaal.ps4remotepkginstaller.Settings;
import com.benjaminfaal.ps4remotepkginstaller.model.ManualConsole;
import com.benjaminfaal.ps4remotepkginstaller.service.ManualConsoleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@CommonsLog
@Service
public class ManualConsoleServiceImpl implements ManualConsoleService, InitializingBean {

    @Autowired
    private Settings settings;

    private final List<ManualConsole> manualConsoles = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (settings.containsKey("manualConsoles")) {
            try {
                TypeReference<List<ManualConsole>> manualConsolesTypeReference = new TypeReference<List<ManualConsole>>() {};
                List<ManualConsole> previousManualConsoles = objectMapper.readValue(settings.getProperty("manualConsoles"), manualConsolesTypeReference);
                manualConsoles.addAll(previousManualConsoles);
            } catch (JsonProcessingException e) {
                log.error("Error loading previous manual consoles", e);
            }
        }
    }

    @Override
    public List<ManualConsole> get() {
        return new ArrayList<>(manualConsoles);
    }

    @Override
    public ManualConsole add(String host) {
        ManualConsole manualConsole = new ManualConsole();
        manualConsole.setHost(host);
        manualConsole.setStatus(Status.UNKNOWN);
        HashMap<String, String> data = new HashMap<>();
        data.put("host-id", UUID.randomUUID().toString());
        manualConsole.setData(data);
        manualConsoles.add(manualConsole);
        saveManualConsoles();
        return manualConsole;
    }

    @Override
    public void edit(ManualConsole manualConsole, String newHost) {
        manualConsole.setHost(newHost);
        saveManualConsoles();
    }

    @Override
    public void remove(ManualConsole manualConsole) {
        manualConsoles.remove(manualConsole);
        saveManualConsoles();
    }

    private void saveManualConsoles() {
        try {
            settings.setProperty("manualConsoles", objectMapper.writeValueAsString(manualConsoles));
        } catch (JsonProcessingException e) {
            log.error("Error saving manual consoles");
        }
    }

}
