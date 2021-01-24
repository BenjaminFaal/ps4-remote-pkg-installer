package com.benjaminfaal.ps4remotepkginstaller.service;

import com.benjaminfaal.ps4remotepkginstaller.model.ManualConsole;

import java.util.List;

public interface ManualConsoleService {

    List<ManualConsole> get();

    ManualConsole add(String host);

    void edit(ManualConsole manualConsole, String newHost);

    void remove(ManualConsole manualConsole);

}
